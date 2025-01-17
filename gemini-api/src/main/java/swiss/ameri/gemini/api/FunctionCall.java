package swiss.ameri.gemini.api;

import java.util.Map;

/**
 * A predicted FunctionCall returned from the model that contains a string representing the FunctionDeclaration.name
 * with the arguments and their values.
 *
 * @param name   Required. The name of the function to call. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 63.
 * @param format Optional. The function parameters and values in JSON object format.
 */
public record FunctionCall(
        String name,
        Map<String, ?> format
) {
}
