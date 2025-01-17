package swiss.ameri.gemini.api;

/**
 * Structured representation of a function declaration as defined by the OpenAPI 3.03 specification.
 * Included in this declaration are the function name and parameters.
 * This FunctionDeclaration is a representation of a block of code that can be used as a Tool by the model and executed by the client.
 *
 * @param name        Required. The name of the function. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 63.
 * @param description Required. A brief description of the function.
 * @param parameters  Optional. Describes the parameters to this function.
 *                    Reflects the Open API 3.03 Parameter Object string Key: the name of the parameter.
 *                    Parameter names are case-sensitive.
 *                    Schema Value: the Schema defining the type used for the parameter.
 */
public record FunctionDeclaration(
        String name,
        String description,
        Schema parameters
) {
}
