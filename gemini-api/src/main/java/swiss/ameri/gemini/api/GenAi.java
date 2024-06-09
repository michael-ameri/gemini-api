package swiss.ameri.gemini.api;

import swiss.ameri.gemini.spi.JsonParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

// todo decide if thread-safe or not, once responses are stored

/**
 * Entry point for all interactions with Gemini API.
 */
public class GenAi {

    private static final String STREAM_LINE_PREFIX = "data: ";
    private static final int STREAM_LINE_PREFIX_LENGTH = STREAM_LINE_PREFIX.length();

    private final String urlPrefix = "https://generativelanguage.googleapis.com/v1beta";

    private final String apiKey;
    private final HttpClient client;
    private final JsonParser jsonParser;

    public GenAi(
            String apiKey,
            JsonParser jsonParser
    ) {
        this(
                apiKey,
                jsonParser,
                HttpClient.newBuilder().build()
        );
    }

    public GenAi(
            String apiKey,
            JsonParser jsonParser,
            HttpClient client
    ) {
        this.apiKey = apiKey;
        this.jsonParser = jsonParser;
        this.client = client;
    }

    /**
     * List models that are currently available.
     */
    public List<Model> listModels() {
        return execute(() -> {
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create("%s/models?key=%s".formatted(urlPrefix, apiKey)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return jsonParser.fromJson(response.body(), ModelResponse.class)
                    .models();
        });
    }

    /**
     * Get information of a model. Can be used to create a {@link GenerativeModel}.
     *
     * @param model of which the information is wanted.
     * @see #listModels()
     */
    public Model getModel(ModelVariant model) {
        return getModel(model.variant());
    }

    /**
     * Get model information.
     *
     * @param model must start with "models/"
     */
    public Model getModel(String model) {
        return execute(() -> {
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create("%s/%s?key=%s".formatted(urlPrefix, model, apiKey)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            return jsonParser.fromJson(response.body(), Model.class);
        });
    }

    /**
     * Generates a response from Gemini API based on the given {@code model}. The response is streamed in chunks of text. The
     * stream items are delivered as they arrive.
     *
     * @param model with the necessary information for Gemini API to generate content
     * @return A live stream of the response, as it arrives
     * @see #generateContent(GenerativeModel) which returns the whole response at once (asynchronously)
     */
    public Stream<GeneratedContent> generateContentStream(GenerativeModel model) {
        // todo, keep responses in the state.
        //  add up the usageMetadata
        //  store the safety ratings
        return execute(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(
                            jsonParser.toJson(convert(model))
                    ))
                    .uri(URI.create("%s/%s:streamGenerateContent?alt=sse&key=%s".formatted(urlPrefix, model.modelName(), apiKey)))
                    .build();

            HttpResponse<Stream<String>> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofLines()
            );
            return response.body()
                    .filter(l -> l.length() > STREAM_LINE_PREFIX_LENGTH)
                    .map(line -> {
                        try {
                            var gcr = jsonParser.fromJson(line.substring(STREAM_LINE_PREFIX_LENGTH), GenerateContentResponse.class);
                            return new GeneratedContent(gcr.candidates().get(0).content().parts().get(0).text());
                        } catch (Exception e) {
                            throw new RuntimeException("Unexpected line:\n" + line, e);
                        }
                    });
        });
    }

    /**
     * Generates a response from Gemini API based on the given {@code model}.
     *
     * @param model with the necessary information for Gemini API to generate content
     * @return a {@link CompletableFuture} which completes once the response from Gemini API has arrived. The {@link CompletableFuture}
     * fails, if an unexpected response returns (e.g. invalid token or parameters are used)
     * @see #generateContentStream(GenerativeModel) to stream the response in chunks, instead of receiving all at once
     */
    public CompletableFuture<GeneratedContent> generateContent(GenerativeModel model) {
        return execute(() -> {
            CompletableFuture<HttpResponse<String>> response = client.sendAsync(
                    HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    jsonParser.toJson(convert(model))
                            ))
                            .uri(URI.create("%s/%s:generateContent?key=%s".formatted(urlPrefix, model.modelName(), apiKey)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            return response
                    .thenApply(HttpResponse::body)
                    .thenApply(body -> {
                        try {
                            var gcr = jsonParser.fromJson(body, GenerateContentResponse.class);
                            return new GeneratedContent(gcr.candidates().get(0).content().parts().get(0).text());
                        } catch (Exception e) {
                            throw new RuntimeException("Unexpected body:\n" + body, e);
                        }
                    });
        });
    }

    private static GenerateContentRequest convert(GenerativeModel model) {
        List<GenerationContent> generationContents = model.contents().stream()
                .map(content -> {
                    // todo change to "switch" over sealed type with jdk 21
                    if (content instanceof Content.TextContent textContent) {
                        return new GenerationContent(
                                textContent.role(),
                                List.of(
                                        new GenerationPart(
                                                textContent.text(),
                                                null
                                        )
                                )
                        );
                    } else if (content instanceof Content.MediaContent imageContent) {
                        return new GenerationContent(
                                imageContent.role(),
                                List.of(
                                        new GenerationPart(
                                                null,
                                                new InlineData(
                                                        imageContent.media().mimeType(),
                                                        imageContent.media().mediaBase64()
                                                )
                                        )
                                )
                        );
                    } else if (content instanceof Content.TextAndMediaContent textAndImagesContent) {
                        return new GenerationContent(
                                textAndImagesContent.role(),
                                Stream.concat(
                                        Stream.of(
                                                new GenerationPart(
                                                        textAndImagesContent.text(),
                                                        null
                                                )
                                        ),
                                        textAndImagesContent.media().stream()
                                                .map(imageData -> new GenerationPart(
                                                        null,
                                                        new InlineData(
                                                                imageData.mimeType(),
                                                                imageData.mediaBase64()
                                                        )
                                                ))
                                ).toList()
                        );
                    } else {
                        throw new RuntimeException("Unexpected content:\n" + content);
                    }
                })
                .toList();
        return new GenerateContentRequest(generationContents, model.safetySettings(), model.generationConfig());
    }

    private <T> T execute(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Content generated by Gemini API.
     */
    public record GeneratedContent(
            String text
    ) {
    }

    private record GenerateContentResponse(
            UsageMetadata usageMetadata,
            List<ResponseCandidate> candidates
    ) {
    }

    private record ResponseCandidate(
            GenerationContent content,
            String finishReason,
            int index,
            List<SafetyRating> safetyRatings
    ) {
    }

    private record SafetyRating(
            String category,
            String probability
    ) {
    }

    private record UsageMetadata(
            int promptTokenCount,
            int candidatesTokenCount,
            int totalTokenCount
    ) {
    }

    private record GenerateContentRequest(
            List<GenerationContent> contents,
            List<SafetySetting> safetySettings,
            GenerationConfig generationConfig
    ) {
    }

    private record GenerationContent(
            String role,
            List<GenerationPart> parts
    ) {
    }

    private record GenerationPart(
            // contains one or the other
            String text,
            InlineData inline_data
    ) {
    }

    private record InlineData(
            String mime_type,
            String data
    ) {
    }

    private record ModelResponse(List<Model> models) {
    }

    public record Model(
            String name,
            String version,
            String displayName,
            String description,
            int inputTokenLimit,
            int outputTokenLimit,
            List<String> supportedGenerationMethods,
            double temperature,
            double topP,
            int topK
    ) {
    }

    private interface ThrowingSupplier<T> {
        T get() throws IOException, InterruptedException;
    }
}
