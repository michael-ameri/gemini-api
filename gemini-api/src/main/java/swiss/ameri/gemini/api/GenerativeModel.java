package swiss.ameri.gemini.api;

import java.util.List;

public record GenerativeModel(
        String modelName,
        List<Content> contents,
        List<SafetySetting> safetySettings,
        GenerationConfig generationConfig
) {

    public static GenerativeModel of(
            ModelVariant modelVariant,
            List<Content> contents,
            List<SafetySetting> safetySettings,
            GenerationConfig generationConfig
    ) {
        // todo add builder, which accepts modelVariant
        return new GenerativeModel(modelVariant.variant(), contents, safetySettings, generationConfig);
    }

}
