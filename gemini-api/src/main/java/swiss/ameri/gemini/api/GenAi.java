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

    public Model getModel(ModelVariant model) {
        // todo return GenerativeModelBuilder
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


    public Stream<GeneratedContent> generateContentStream(GenerativeModel model) {
        // todo, keep responses in the state.
        //  add up the usageMetadata
        //  store the safety ratings
        return execute(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(
                            jsonParser.toJson(convert(model.contents()))
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

    public CompletableFuture<GeneratedContent> generateContent(GenerativeModel model) {
        return execute(() -> {
            CompletableFuture<HttpResponse<String>> response = client.sendAsync(
                    HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    jsonParser.toJson(convert(model.contents()))
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

    private static GenerateContentRequest convert(List<Content> contents) {
        // todo add safetySettings, generationConfig
        List<GenerationContent> generationContents = contents.stream()
                .map(content -> {
                    // todo change to "switch" over sealed type with jdk 21
                    if (content instanceof Content.TextContent textContent) {
                        return new GenerationContent(
                                textContent.role(),
                                List.of(
                                        new GenerationPart(
                                                textContent.text(),
                                                null,
                                                null
                                        )
                                )
                        );
                    } else if (content instanceof Content.ImageContent imageContent) {
                        return new GenerationContent(
                                imageContent.role(),
                                List.of(
                                        new GenerationPart(
                                                null,
                                                imageContent.image().mimeType(),
                                                imageContent.image().imageBase64()
                                        )
                                )
                        );
                    } else if (content instanceof Content.TextAndImagesContent textAndImagesContent) {
                        return new GenerationContent(
                                textAndImagesContent.role(),
                                Stream.concat(
                                        Stream.of(
                                                new GenerationPart(
                                                        textAndImagesContent.text(),
                                                        null,
                                                        null
                                                )
                                        ),
                                        textAndImagesContent.images().stream()
                                                .map(imageData -> new GenerationPart(
                                                        null,
                                                        imageData.mimeType(),
                                                        imageData.imageBase64()
                                                ))
                                ).toList()
                        );
                    } else {
                        throw new RuntimeException("Unexpected content:\n" + content);
                    }
                })
                .toList();
        return new GenerateContentRequest(generationContents);
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
            List<GenerationContent> contents
    ) {
    }

    private record GenerationContent(
            String role,
            List<GenerationPart> parts
    ) {
    }

    private record GenerationPart(
            String text,
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
