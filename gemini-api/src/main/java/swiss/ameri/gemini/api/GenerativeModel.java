package swiss.ameri.gemini.api;

import java.util.ArrayList;
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

    /**
     * Create a {@link GenerativeModelBuilder}.
     *
     * @return an empty {@link GenerativeModelBuilder}
     */
    public static GenerativeModelBuilder builder() {
        return new GenerativeModelBuilder();
    }

    /**
     * A builder for {@link GenerativeModel}. Currently, does not validate the fields when building the model. Not thread-safe.
     */
    public static class GenerativeModelBuilder {
        private String modelName;
        private GenerationConfig generationConfig;
        private final List<Content> contents = new ArrayList<>();
        private final List<SafetySetting> safetySettings = new ArrayList<>();

        private GenerativeModelBuilder() {
        }

        /**
         * Set the model name.
         *
         * @param modelName to be set
         * @return this
         */
        public GenerativeModelBuilder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        /**
         * Set the model name.
         *
         * @param modelVariant to be set
         * @return this
         */
        public GenerativeModelBuilder modelName(ModelVariant modelVariant) {
            return modelName(modelVariant == null ? null : modelVariant.variant());
        }

        /**
         * Add content
         *
         * @param content to be added
         * @return this
         */
        public GenerativeModelBuilder addContent(Content content) {
            this.contents.add(content);
            return this;
        }

        /**
         * Add safety setting
         *
         * @param safetySetting to be added
         * @return this
         */
        public GenerativeModelBuilder addSafetySetting(SafetySetting safetySetting) {
            this.safetySettings.add(safetySetting);
            return this;
        }

        /**
         * Set the generation config
         *
         * @param generationConfig to be set
         * @return this
         */
        public GenerativeModelBuilder generationConfig(GenerationConfig generationConfig) {
            this.generationConfig = generationConfig;
            return this;
        }

        /**
         * Build the model based on this builder.
         *
         * @return a completed (not necessarily validated) {@link GenerativeModel}
         */
        public GenerativeModel build() {
            return new GenerativeModel(modelName, contents, safetySettings, generationConfig);
        }
    }

}
