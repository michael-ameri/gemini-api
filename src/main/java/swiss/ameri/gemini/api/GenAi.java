package swiss.ameri.gemini.api;

import swiss.ameri.gemini.spi.JsonParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GenAi {

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
        return getModel(model.variant());
    }

    public Model getModel(String model) {
        return execute(() -> {
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create("%s/models/%s?key=%s".formatted(urlPrefix, model, apiKey)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return jsonParser.fromJson(response.body(), Model.class);
        });
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
