package swiss.ameri.gemini.api;

/**
 * (Potentially non-exhaustive) list of supported models.
 *
 * @see <a href="https://ai.google.dev/gemini-api/docs/models/gemini">Gemini Models</a>
 */
public enum ModelVariant {
    GEMINI_1_5_PRO("gemini-1.5-pro"),
    GEMINI_1_5_FLASH("gemini-1.5-flash"),
    GEMINI_1_0_PRO("gemini-1.0-pro"),
    GEMINI_1_0_PRO_VISION("gemini-pro-vision"),
    TEXT_EMBEDDING_004("text-embedding-004"),
    ;

    private final String variant;

    ModelVariant(String variant) {
        this.variant = variant;
    }

    public String variant() {
        return "models/" + variant;
    }
}
