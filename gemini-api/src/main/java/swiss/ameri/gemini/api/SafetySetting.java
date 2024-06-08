package swiss.ameri.gemini.api;

public record SafetySetting(
        String category,
        String threshold
) {

    public static SafetySetting of(
            HarmCategory category,
            HarmBlockThreshold threshold
    ) {
        return new SafetySetting(
                category.name(),
                threshold.name()
        );
    }

    /**
     * According to <a href="https://ai.google.dev/api/rest/v1beta/HarmCategory">HarmCategory</a>.
     * Currently, only the first 4 seem to be recognized.
     */
    public enum HarmCategory {
        HARM_CATEGORY_HATE_SPEECH,
        HARM_CATEGORY_SEXUALLY_EXPLICIT,
        HARM_CATEGORY_DANGEROUS_CONTENT,
        HARM_CATEGORY_HARASSMENT,
        HARM_CATEGORY_SEXUAL,
        HARM_CATEGORY_MEDICAL,
        HARM_CATEGORY_DANGEROUS,
    }

    /**
     * According to <a href="https://ai.google.dev/api/rest/v1beta/SafetySetting#harmblockthreshold">SafetySetting</a>
     */
    public enum HarmBlockThreshold {
        HARM_BLOCK_THRESHOLD_UNSPECIFIED,
        BLOCK_LOW_AND_ABOVE,
        BLOCK_MEDIUM_AND_ABOVE,
        BLOCK_ONLY_HIGH,
        BLOCK_NONE
    }

}
