package swiss.ameri.gemini.gson;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import swiss.ameri.gemini.api.Schema;
import swiss.ameri.gemini.spi.JsonParser;

/**
 * Reference implementation of {@link JsonParser} using {@link Gson} dependency.
 */
public class GsonJsonParser implements JsonParser {

    /**
     * Field naming strategy to avoid usage of illegal field names in java.
     * See e.g. {@link Schema#ameri_swiss_enum()}, which cannot be named {@code enum}.
     */
    public static final FieldNamingStrategy FIELD_NAMING_STRATEGY = field -> {
        if (field.getName().startsWith("ameri_swiss_")) {
            return field.getName().substring("ameri_swiss_".length());
        }
        return field.getName();
    };

    private final Gson gson;

    /**
     * Create a {@link JsonParser} with a custom {@link Gson}.
     *
     * @param gson instance to use
     */
    public GsonJsonParser(Gson gson) {
        this.gson = gson;
    }

    /**
     * Create a default {@link JsonParser} instance.
     */
    public GsonJsonParser() {
        this(new GsonBuilder().setFieldNamingStrategy(FIELD_NAMING_STRATEGY).create());
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
