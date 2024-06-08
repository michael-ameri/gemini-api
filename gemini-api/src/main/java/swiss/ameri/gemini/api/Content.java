package swiss.ameri.gemini.api;

import java.util.List;
import java.util.Locale;

public sealed interface Content {

    // todo add builder (with support for role enum)

    record TextContent(
            String role,
            String text
    ) implements Content {
    }

    record MediaContent(
            String role,
            MediaData media
    ) implements Content {
        // todo test
    }

    record TextAndMediaContent(
            String role,
            String text,
            List<MediaData> media
    ) implements Content {
        // todo test
    }

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
