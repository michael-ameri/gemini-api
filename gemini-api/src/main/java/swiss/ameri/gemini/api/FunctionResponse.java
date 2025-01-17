package swiss.ameri.gemini.api;

import java.util.Map;

/**
 * The result output from a FunctionCall that contains a string representing the FunctionDeclaration.name and
 * a structured JSON object containing any output from the function is used as context to the model.
 * This should contain the result of aFunctionCall made based on model prediction.
 *
 * @param name     Required. The name of the function to call. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 63.
 * @param response Required. The function response in JSON object format.
 */
public record FunctionResponse(
        String name,
        Map<String, ?> response
) {
}
