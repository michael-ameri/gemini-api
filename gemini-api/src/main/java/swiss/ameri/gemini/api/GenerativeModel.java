package swiss.ameri.gemini.api;

import java.util.List;

public record GenerativeModel(
        String modelName,
        List<Content> contents,
        List<SafetySetting> safetySettings
) {

    public static GenerativeModel of(
            ModelVariant modelVariant,
            List<Content> contents,
            List<SafetySetting> safetySettings
    ) {
        // todo add builder, which accepts modelVariant
        return new GenerativeModel(modelVariant.variant(), contents, safetySettings);
    }

}
