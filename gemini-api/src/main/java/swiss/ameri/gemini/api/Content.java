package swiss.ameri.gemini.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Content which can be sent to gemini API.
 */
public sealed interface Content {

    /**
     * Role belonging to this turn in the conversation.
     *
     * @return string value of a {@link Role}
     */
    String role();

    /**
     * Create a {@link FunctionCallContent}.
     *
     * @param role         belonging to this turn in the conversation.
     * @param functionCall by the role
     * @return a {@link FunctionCallContent}
     */
    static FunctionCallContent functionCallContent(
            Role role,
            FunctionCall functionCall
    ) {
        return new FunctionCallContent(role == null ? null : role.roleName(), functionCall);
    }

    /**
     * Create a {@link FunctionCallContent}.
     *
     * @param role             belonging to this turn in the conversation.
     * @param functionResponse by the role
     * @return a {@link FunctionResponseContent}
     */
    static FunctionResponseContent functionResponseContent(
            Role role,
            FunctionResponse functionResponse
    ) {
        return new FunctionResponseContent(role == null ? null : role.roleName(), functionResponse);
    }

    /**
     * Create a {@link TextContent}.
     *
     * @param role belonging to this turn in the conversation.
     * @param text by the role
     * @return a {@link TextContent}
     */
    static TextContent textContent(
            Role role,
            String text
    ) {
        return new TextContent(role == null ? null : role.roleName(), text);
    }

    /**
     * Create a {@link MediaContent}.
     *
     * @param role        belonging to this turn in the conversation.
     * @param mimeType    see {@link MediaData}
     * @param mediaBase64 see {@link MediaData}
     * @return a {@link MediaContent}
     */
    static MediaContent mediaContent(
            Role role,
            String mimeType,
            String mediaBase64

    ) {
        return new MediaContent(role == null ? null : role.roleName(), new MediaData(mimeType, mediaBase64));
    }

    /**
     * For combined text and media content in one turn
     *
     * @return a {@link TextAndMediaContent.TextAndMediaContentBuilder}
     */
    static TextAndMediaContent.TextAndMediaContentBuilder textAndMediaContentBuilder() {
        return TextAndMediaContent.builder();
    }

    /**
     * A predicted FunctionCall returned from the model that contains a string representing the FunctionDeclaration.name
     * with the arguments and their values.
     *
     * @param role         belonging to this turn in the conversation. see {@link Role}
     * @param functionCall returned form the model
     */
    record FunctionCallContent(
            String role,
            FunctionCall functionCall
    ) implements Content {
    }

    /**
     * The result output from a FunctionCall that contains a string representing the FunctionDeclaration.name and
     * a structured JSON object containing any output from the function is used as context to the model.
     * This should contain the result of aFunctionCall made based on model prediction.
     *
     * @param role             belonging to this turn in the conversation. see {@link Role}
     * @param functionResponse response to a function call
     */
    record FunctionResponseContent(
            String role,
            FunctionResponse functionResponse
    ) implements Content {
    }

    /**
     * A part of a conversation that contains text.
     *
     * @param role belonging to this turn in the conversation. see {@link Role}
     * @param text by the role
     */
    record TextContent(
            String role,
            String text
    ) implements Content {
    }

    /**
     * A part of a conversation that contains media.
     *
     * @param role  belonging to this turn in the conversation. see {@link Role}
     * @param media data
     */
    record MediaContent(
            String role,
            MediaData media
    ) implements Content {
    }

    /**
     * A part of a conversation that contains text and media.
     *
     * @param role  belonging to this turn in the conversation. see {@link Role}
     * @param text  of the conversation part
     * @param media the medias
     */
    record TextAndMediaContent(
            String role,
            String text,
            List<MediaData> media
    ) implements Content {

        /**
         * Create a {@link TextAndMediaContentBuilder}.
         *
         * @return a new builder
         */
        public static TextAndMediaContentBuilder builder() {
            return new TextAndMediaContentBuilder();
        }


        /**
         * Builder for {@link TextAndMediaContent}.
         */
        public static class TextAndMediaContentBuilder {
            private String role;
            private String text;
            private final List<MediaData> media = new ArrayList<>();

            private TextAndMediaContentBuilder() {
            }

            /**
             * Set the role
             *
             * @param role to set
             * @return this
             * @see #role(Role)
             */
            public TextAndMediaContentBuilder role(String role) {
                this.role = role;
                return this;
            }

            /**
             * Set the role
             *
             * @param role to set
             * @return this
             */
            public TextAndMediaContentBuilder role(Role role) {
                return role(role == null ? null : role.roleName());
            }

            /**
             * Set the text
             *
             * @param text to set
             * @return this
             */
            public TextAndMediaContentBuilder text(String text) {
                this.text = text;
                return this;
            }

            /**
             * Add media
             *
             * @param media to add
             * @return this
             */
            public TextAndMediaContentBuilder addMedia(MediaData media) {
                this.media.add(media);
                return this;
            }

            /**
             * Create the result. It is not validated.
             *
             * @return a newly built {@link TextAndMediaContent}
             */
            public TextAndMediaContent build() {
                return new TextAndMediaContent(role, text, media);
            }
        }

    }

    /**
     * Media used during a conversion.
     *
     * @param mimeType    e.g. image/jpeg
     * @param mediaBase64 the media, base64 encoded
     */
    record MediaData(
            String mimeType,
            String mediaBase64
    ) {
    }

    /**
     * The producer of the content. Mainly relevant for multi turn conversations.
     */
    enum Role {
        /**
         * Content by user.
         */
        USER,
        /**
         * Content by model.
         */
        MODEL;

        private final String roleName;

        Role() {
            this.roleName = name().toLowerCase(Locale.ROOT);
        }

        /**
         * Role name for the api
         *
         * @return Role name for the api
         */
        public String roleName() {
            return roleName;
        }
    }
}
