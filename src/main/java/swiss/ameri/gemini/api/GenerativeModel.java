package swiss.ameri.gemini.api;

public record GenerativeModel(
        String modelName
) {

    public static GenerativeModel of(
            ModelVariant modelVariant
    ) {
        // todo add builder, which accepts modelVariant
        return new GenerativeModel(modelVariant.variant());
    }

}
