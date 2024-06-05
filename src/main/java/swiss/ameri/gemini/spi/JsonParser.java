package swiss.ameri.gemini.spi;

public interface JsonParser {

    String toJson(Object object);

    <T> T fromJson(String json, Class<T> clazz);

}
