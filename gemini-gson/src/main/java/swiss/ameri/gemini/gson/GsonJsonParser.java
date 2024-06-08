package swiss.ameri.gemini.gson;

import com.google.gson.Gson;
import swiss.ameri.gemini.spi.JsonParser;

/**
 * Reference implementation of {@link JsonParser} using {@link Gson} dependency.
 */
public class GsonJsonParser implements JsonParser {

    private final Gson gson;

    /**
     * Create a {@link JsonParser} with a custom {@link Gson}.
     */
    public GsonJsonParser(Gson gson) {
        this.gson = gson;
    }

    /**
     * Create a default {@link JsonParser} instance.
     */
    public GsonJsonParser() {
        this(new Gson());
    }

    @Override
    public String toJson(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
