package swiss.ameri.gemini.api;

public record GenerativeModel(
        String modelName,
        String apiKey
) {

    public static GenerativeModel of(
            ModelVariant modelVariant,
            String apiKey
    ) {
        return new GenerativeModel(modelVariant.variant(), apiKey);
    }

}
