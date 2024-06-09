package swiss.ameri.gemini.api;

import swiss.ameri.gemini.spi.JsonParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;


/**
 * Entry point for all interactions with Gemini API.
 * Note that some methods store state (e.g. {@link #generateContent(GenerativeModel)} or ${@link #generateContentStream(GenerativeModel)}).
 * Call the {@link #close()} method to clean up the state.
 * This class is thread safe.
 */
public class GenAi implements AutoCloseable {

    private static final String STREAM_LINE_PREFIX = "data: ";
    private static final int STREAM_LINE_PREFIX_LENGTH = STREAM_LINE_PREFIX.length();

    private final String urlPrefix = "https://generativelanguage.googleapis.com/v1beta";

    private final String apiKey;
    private final HttpClient client;
    private final JsonParser jsonParser;
    private final Map<UUID, GenerateContentResponse> responseById = new ConcurrentHashMap<>();

    /**
     * Create a new instance with a default {@link HttpClient}
     *
     * @param apiKey     to be used for all communications with Gemini API
     * @param jsonParser used to (de-)serialize JSON objects
     */
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

    /**
     * Create a new instance
     *
     * @param apiKey     to be used for all communications with Gemini API
     * @param jsonParser used to (de-)serialize JSON objects
     * @param client     a custom {@link HttpClient} for communication with Gemini API
     */
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
     *
     * @return available models
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
     * @return information of a model
     * @see #listModels()
     */
    public Model getModel(ModelVariant model) {
        return getModel(model.variant());
    }

    /**
     * Get model information.
     *
     * @param model must start with "models/"
     * @return information of a model
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
     * Get the usage metadata of a {@link GeneratedContent#id()}.
     *
     * @param id of the corresponding {@link GeneratedContent}
     * @return the corresponding metadata, or an empty optional
     */
    public Optional<UsageMetadata> usageMetadata(UUID id) {
        return Optional.ofNullable(responseById.get(id))
                .map(GenerateContentResponse::usageMetadata);
    }

    /**
     * Get the safety ratings of a {@link GeneratedContent#id()}.
     *
     * @param id of the corresponding {@link GeneratedContent}
     * @return the corresponding safety ratings, or an empty optional
     */
    public List<SafetyRating> safetyRatings(UUID id) {
        GenerateContentResponse response = responseById.get(id);
        if (response == null) {
            return emptyList();
        }
        return response.candidates().stream()
                .flatMap(candidate -> candidate.safetyRatings().stream())
                .toList();
    }


    /**
     * Runs a model's tokenizer on input content and returns the token count.
     * When using long prompts, it might be useful to count tokens before sending any content to the model.
     *
     * @param model to be analyzed
     * @return the token count
     */
    public CompletableFuture<Long> countTokens(GenerativeModel model) {
        return execute(() -> {
            CompletableFuture<HttpResponse<String>> response = client.sendAsync(
                    HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    jsonParser.toJson(new CountTokenRequest(convert(model)))
                            ))
                            .uri(URI.create("%s/%s:countTokens?key=%s".formatted(urlPrefix, model.modelName(), apiKey)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            return response
                    .thenApply(HttpResponse::body)
                    .thenApply(body -> {
                        try {
                            var ctr = jsonParser.fromJson(body, CountTokenResponse.class);
                            if (ctr.totalTokens() == null) {
                                throw new RuntimeException("No token field in response");
                            }
                            return ctr.totalTokens();
                        } catch (Exception e) {
                            throw new RuntimeException("Unexpected body:\n" + body, e);
                        }
                    });
        });
    }

    /**
     * Generates a response from Gemini API based on the given {@code model}. The response is streamed in chunks of text. The
     * stream items are delivered as they arrive.
     * Once the call has been completed, metadata and safety ratings can be obtained by calling
     * {@link #usageMetadata(UUID)} or {@link #safetyRatings(UUID)}. If those methods are called while the stream is still
     * active, the last available statistics are returned.
     *
     * @param model with the necessary information for Gemini API to generate content
     * @return A live stream of the response, as it arrives
     * @see #generateContent(GenerativeModel) which returns the whole response at once (asynchronously)
     */
    public Stream<GeneratedContent> generateContentStream(GenerativeModel model) {
        return execute(() -> {
            UUID uuid = UUID.randomUUID();
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
                            // each element can just replace the previous one
                            this.responseById.put(uuid, gcr);
                            return new GeneratedContent(uuid, gcr.candidates().get(0).content().parts().get(0).text());
                        } catch (Exception e) {
                            throw new RuntimeException("Unexpected line:\n" + line, e);
                        }
                    });
        });
    }

    /**
     * Generates a response from Gemini API based on the given {@code model}.
     * Once the call has been completed, metadata and safety ratings can be obtained by calling
     * {@link #usageMetadata(UUID)} or {@link #safetyRatings(UUID)}
     *
     * @param model with the necessary information for Gemini API to generate content
     * @return a {@link CompletableFuture} which completes once the response from Gemini API has arrived. The {@link CompletableFuture}
     * fails, if an unexpected response returns (e.g. invalid token or parameters are used)
     * @see #generateContentStream(GenerativeModel) to stream the response in chunks, instead of receiving all at once
     */
    public CompletableFuture<GeneratedContent> generateContent(GenerativeModel model) {
        return execute(() -> {
            UUID uuid = UUID.randomUUID();
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
                            responseById.put(uuid, gcr);
                            return new GeneratedContent(uuid, gcr.candidates().get(0).content().parts().get(0).text());
                        } catch (Exception e) {
                            throw new RuntimeException("Unexpected body:\n" + body, e);
                        }
                    });
        });
    }

    /**
     * Embedding is a technique used to represent information as a list of floating point numbers in an array.
     * With Gemini, you can represent text (words, sentences, and blocks of text) in a vectorized form,
     * making it easier to compare and contrast embeddings.
     * For example, two texts that share a similar subject or sentiment should have similar embeddings,
     * which can be identified through mathematical comparison techniques such as cosine similarity.
     *
     * @param model                to use. Currently, only {@link ModelVariant#TEXT_EMBEDDING_004} is allowed.
     * @param taskType             Optional. Optional task type for which the embeddings will be used. For possible values, see {@link TaskType}
     * @param title                Optional. An optional title for the text. Only applicable when TaskType is RETRIEVAL_DOCUMENT.
     *                             Note: Specifying a title for RETRIEVAL_DOCUMENT provides better quality embeddings for retrieval.
     * @param outputDimensionality Optional. Optional reduced dimension for the output embedding.
     *                             If set, excessive values in the output embedding are truncated from the end.
     *                             Supported by newer models since 2024, and the earlier model (models/embedding-001) cannot specify this value.
     * @return List of values
     * @apiNote Only {@link swiss.ameri.gemini.api.Content.TextContent} are allowed.
     */
    public CompletableFuture<List<ContentEmbedding>> embedContents(
            GenerativeModel model,
            String taskType,
            String title,
            Long outputDimensionality
    ) {
        return execute(() -> {

            var requests = convertGenerationContents(model)
                    .stream()
                    .map(generationContent -> new EmbedContentRequest(
                            model.modelName(),
                            generationContent,
                            taskType,
                            title,
                            outputDimensionality
                    ))
                    .toList();

            var request = new BatchEmbedContentRequest(requests);

            CompletableFuture<HttpResponse<String>> response = client.sendAsync(
                    HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    jsonParser.toJson(request)
                            ))
                            .uri(URI.create("%s/%s:batchEmbedContents?key=%s".formatted(urlPrefix, model.modelName(), apiKey)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            return response
                    .thenApply(HttpResponse::body)
                    .thenApply(body -> {
                        try {
                            BatchEmbedContentResponse becr = jsonParser.fromJson(body, BatchEmbedContentResponse.class);
                            if (becr.embeddings() == null) {
                                throw new RuntimeException();
                            }
                            return becr
                                    .embeddings();
                        } catch (Exception e) {
                            throw new RuntimeException("Unexpected body:\n" + body, e);
                        }
                    });

        });
    }

    private static GenerateContentRequest convert(GenerativeModel model) {
        List<GenerationContent> generationContents = convertGenerationContents(model);
        return new GenerateContentRequest(model.modelName(), generationContents, model.safetySettings(), model.generationConfig());
    }

    private static List<GenerationContent> convertGenerationContents(GenerativeModel model) {
        return model.contents().stream()
                .map(content -> {
                    // change to "switch" over sealed type with jdk 21
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
     * Clears the internal state.
     */
    @Override
    public void close() {
        responseById.clear();
    }

    /**
     * Content generated by Gemini API.
     *
     * @param id   the id of the request, for subsequent queries regarding metadata of the query
     * @param text of the generated content
     */
    public record GeneratedContent(
            UUID id,
            String text
    ) {
    }

    /**
     * Usage metadata for a given request.
     *
     * @param promptTokenCount     Number of tokens in the prompt.
     * @param candidatesTokenCount Total number of tokens for the generated response.
     * @param totalTokenCount      Total token count for the generation request (prompt + candidates).
     */
    public record UsageMetadata(
            int promptTokenCount,
            int candidatesTokenCount,
            int totalTokenCount
    ) {
    }

    /**
     * Safety rating for a given response.
     *
     * @param category    The category for this rating. see {@link swiss.ameri.gemini.api.SafetySetting.HarmCategory}
     * @param probability The probability of harm for this content. see {@link swiss.ameri.gemini.api.SafetySetting.HarmProbability}
     */
    public record SafetyRating(
            String category,
            String probability
    ) {

        /**
         * Convert the safety rating to a typed safety rating.
         * Might crash if Gemini API changes, and an enum value is missing.
         *
         * @return the TypedSafetyRating
         */
        public TypedSafetyRating toTypedSafetyRating() {
            return new TypedSafetyRating(
                    SafetySetting.HarmCategory.valueOf(category()),
                    SafetySetting.HarmProbability.valueOf(probability())
            );
        }

        /**
         * Typed values. This is done separately, since enum values might be missing compared to Gemini API
         *
         * @param harmCategory of this rating
         * @param probability  of this rating
         */
        public record TypedSafetyRating(
                SafetySetting.HarmCategory harmCategory,
                SafetySetting.HarmProbability probability
        ) {
        }

    }

    /**
     * A list of floats representing an embedding.
     *
     * @param values A list of floats representing an embedding.
     */
    public record ContentEmbedding(
            List<Double> values
    ) {
    }

    /**
     * Information on a model
     *
     * @param name                       The resource name of the Model.
     *                                   <p>
     *                                   Format: models/{model} with a {model} naming convention of:
     *                                   <p>
     *                                   "{baseModelId}-{version}"
     *                                   Examples:
     *                                   <p>
     *                                   models/chat-bison-001
     * @param baseModelId                The name of the base model, pass this to the generation request.
     * @param version                    The version number of the model.
     * @param displayName                The human-readable name of the model. E.g. "Chat Bison".
     *                                   <p>
     *                                   The name can be up to 128 characters long and can consist of any UTF-8 characters.
     * @param description                A short description of the model.
     * @param inputTokenLimit            Maximum number of input tokens allowed for this model.
     * @param outputTokenLimit           Maximum number of output tokens available for this model.
     * @param supportedGenerationMethods The model's supported generation methods.
     *                                   <p>
     *                                   The method names are defined as Pascal case strings, such as generateMessage which correspond to API methods.
     * @param temperature                Controls the randomness of the output.
     *                                   <p>
     *                                   Values can range over [0.0,2.0], inclusive.
     *                                   A higher value will produce responses that are more varied,
     *                                   while a value closer to 0.0 will typically result in less surprising responses from the model.
     *                                   This value specifies default to be used by the backend while making the call to the model.
     * @param topP                       For Nucleus sampling.
     *                                   <p>
     *                                   Nucleus sampling considers the smallest set of tokens whose probability sum is at least topP.
     *                                   This value specifies default to be used by the backend while making the call to the model.
     * @param topK                       For Top-k sampling.
     *                                   <p>
     *                                   Top-k sampling considers the set of topK most probable tokens.
     *                                   This value specifies default to be used by the backend while making the call to the model.
     *                                   If empty, indicates the model doesn't use top-k sampling, and topK isn't allowed as a generation parameter.
     */
    public record Model(
            String name,
            String baseModelId,
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

    private record BatchEmbedContentRequest(
            List<EmbedContentRequest> requests
    ) {
    }

    private record EmbedContentRequest(
            String model,
            GenerationContent content,
            String taskType,
            String title,
            Long outputDimensionality
    ) {
    }

    private record BatchEmbedContentResponse(
            List<ContentEmbedding> embeddings
    ) {
    }

    private record CountTokenRequest(
            GenerateContentRequest generateContentRequest
    ) {
    }

    private record CountTokenResponse(
            Long totalTokens
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

    private record GenerateContentRequest(
            // for some reason, model is required for countToken, but not for the others.
            // But it seems to be acceptable for the others, so we just add it to all for now
            String model,
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

    private interface ThrowingSupplier<T> {
        T get() throws IOException, InterruptedException;
    }
}
