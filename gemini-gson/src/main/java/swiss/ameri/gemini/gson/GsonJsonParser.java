package swiss.ameri.gemini.gson;

import com.google.gson.Gson;
import swiss.ameri.gemini.spi.JsonParser;

public class GsonJsonParser implements JsonParser {

    private final Gson gson;

    public GsonJsonParser(Gson gson) {
        this.gson = gson;
    }

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
