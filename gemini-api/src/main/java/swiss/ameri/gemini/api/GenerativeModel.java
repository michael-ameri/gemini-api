package swiss.ameri.gemini.api;

import java.util.List;

/**
 * Contains all the information needed for Gemini API to generate new content.
 *
 * @param modelName        to be used. see {@link ModelVariant}. Must start with "models/"
 * @param contents         given as input to Gemini API
 * @param safetySettings   optional, to adjust safety settings
 * @param generationConfig optional, to configure the prompt
 */
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
