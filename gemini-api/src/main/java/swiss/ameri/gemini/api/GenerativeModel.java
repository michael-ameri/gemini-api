package swiss.ameri.gemini.api;

import java.util.List;

public record GenerativeModel(
        String modelName,
        List<Content> contents
) {

    public static GenerativeModel of(
            ModelVariant modelVariant,
            List<Content> contents
    ) {
        // todo add builder, which accepts modelVariant
        return new GenerativeModel(modelVariant.variant(), contents);
    }

}
