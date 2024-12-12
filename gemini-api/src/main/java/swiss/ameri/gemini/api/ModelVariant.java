package swiss.ameri.gemini.api;

/**
 * (Potentially non-exhaustive) list of supported models.
 *
 * @see <a href="https://ai.google.dev/gemini-api/docs/models/gemini">Gemini Models</a>
 */
public enum ModelVariant {
    /**
     * Next generation features, speed, and multimodal generation for a diverse variety of tasks.
     * <ul>
     *     <li>Input: Audio, images, videos, and text</li>
     *     <li>Output: Text, images (coming soon), and audio (coming soon)</li>
     * </ul>
     */
    GEMINI_2_0_FLASH_EXP("gemini-2.0-flash-exp"),
    /**
     * Complex reasoning tasks such as code and text generation, text editing, problem-solving, data extraction and generation.
     */
    GEMINI_1_5_PRO("gemini-1.5-pro"),
    /**
     * Fast and versatile performance across a diverse variety of tasks.
     */
    GEMINI_1_5_FLASH("gemini-1.5-flash"),
    /**
     * High volume and lower intelligence tasks.
     */
    GEMINI_1_5_FLASH_8B("gemini-1.5-flash-8b"),
    /**
     * Natural language tasks, multi-turn text and code chat, and code generation.
     *
     * @deprecated on 2/15/2025
     */
    @Deprecated
    GEMINI_1_0_PRO("gemini-1.0-pro"),
    /**
     * Measuring the relatedness of text strings.
     */
    TEXT_EMBEDDING_004("text-embedding-004"),
    /**
     * Providing source-grounded answers to questions.
     */
    AQA("aqa"),
    ;

    private final String variant;

    ModelVariant(String variant) {
        this.variant = variant;
    }

    /**
     * Model variant name.
     *
     * @return Model variant name as needed by Gemini API
     */
    public String variant() {
        return "models/" + variant;
    }
}
