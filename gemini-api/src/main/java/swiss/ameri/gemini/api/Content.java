package swiss.ameri.gemini.api;

import java.util.List;
import java.util.Locale;

/**
 * Content which can be sent to gemini API.
 */
public sealed interface Content {

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
        // todo test
    }

    /**
     * A part of a conversation that contains text and media.
     *
     * @param role belonging to this turn in the conversation. see {@link Role}
     */
    record TextAndMediaContent(
            String role,
            String text,
            List<MediaData> media
    ) implements Content {
        // todo test
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


    enum Role {
        USER,
        MODEL;

        private final String roleName;

        Role() {
            this.roleName = name().toLowerCase(Locale.ROOT);
        }

        public String roleName() {
            return roleName;
        }
    }
}
