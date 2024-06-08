package swiss.ameri.gemini.api;

import java.util.List;

public record Content(
        String role,
        List<String> texts,
        List<String> images
) {

    // todo add builder
}
