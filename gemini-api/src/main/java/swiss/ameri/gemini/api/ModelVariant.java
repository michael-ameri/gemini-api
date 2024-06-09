package swiss.ameri.gemini.api;

/**
 * (Potentially non-exhaustive) list of supported models.
 *
 * @see <a href="https://ai.google.dev/gemini-api/docs/models/gemini">Gemini Models</a>
 */
public enum ModelVariant {
    /**
     * Complex reasoning tasks such as code and text generation, text editing, problem-solving, data extraction and generation.
     */
    GEMINI_1_5_PRO("gemini-1.5-pro"),
    /**
     * Fast and versatile performance across a diverse variety of tasks.
     */
    GEMINI_1_5_FLASH("gemini-1.5-flash"),
    /**
     * Natural language tasks, multi-turn text and code chat, and code generation.
     */
    GEMINI_1_0_PRO("gemini-1.0-pro"),
    /**
     * Visual-related tasks, like generating image descriptions or identifying objects in images.
     */
    GEMINI_1_0_PRO_VISION("gemini-pro-vision"),
    /**
     * Measuring the relatedness of text strings.
     */
    TEXT_EMBEDDING_004("text-embedding-004"),
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
