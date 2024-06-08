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

    record ImageContent(
            String role,
            ImageData image
    ) implements Content {
        // todo test
    }

    record TextAndImagesContent(
            String role,
            String text,
            List<ImageData> images
    ) implements Content {
        // todo test
    }

    record ImageData(
            String mimeType,
            String imageBase64
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
