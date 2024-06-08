package swiss.ameri.gemini.api;

import java.util.List;

/**
 * According to <a href="https://ai.google.dev/api/rest/v1beta/GenerationConfig">GenerationConfig</a>
 */
public record GenerationConfig(
        List<String> stopSequences,
        String responseMimeType,
        String responseSchema,
        Integer maxOutputTokens,
        Double temperature,
        Double topP,
        Integer topK
) {


}
