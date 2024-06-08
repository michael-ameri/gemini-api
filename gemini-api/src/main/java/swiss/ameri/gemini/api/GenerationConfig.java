package swiss.ameri.gemini.api;

import java.util.List;

/**
 * Generation configuration.
 *
 * @param stopSequences    Optional. The set of character sequences (up to 5) that will stop output generation.
 *                         If specified, the API will stop at the first appearance of a stop sequence.
 *                         The stop sequence will not be included as part of the response.
 * @param responseMimeType Optional. Output response mimetype of the generated candidate text.
 *                         Supported mimetype: text/plain: (default) Text output.
 *                         application/json: JSON response in the candidates.
 * @param responseSchema   Optional. Output response schema of the generated candidate text when response mime type can have schema.
 *                         Schema can be objects, primitives or arrays and is a subset of OpenAPI schema.
 *                         If set, a compatible responseMimeType must also be set. Compatible mimetypes: application/json: Schema for JSON response.
 * @param maxOutputTokens  Optional. The maximum number of tokens to include in a candidate.
 *                         Note: The default value varies by model, see the Model.output_token_limit attribute of the Model returned from the getModel function.
 * @param temperature      Optional. Controls the randomness of the output.
 *                         Note: The default value varies by model, see the Model. temperature attribute of the Model returned from the getModel function.
 *                         Values can range from [0.0, 2.0].
 * @param topP             Optional. The maximum cumulative probability of tokens to consider when sampling.
 *                         The model uses combined Top-k and nucleus sampling.
 *                         Tokens are sorted based on their assigned probabilities so that only the most likely tokens are considered.
 *                         Top-k sampling directly limits the maximum number of tokens to consider, while Nucleus sampling limits number of tokens based on the cumulative probability.
 *                         Note: The default value varies by model, see the Model.top_p attribute of the Model returned from the getModel function.
 * @param topK             Optional. The maximum number of tokens to consider when sampling.
 *                         Models use nucleus sampling or combined Top-k and nucleus sampling.
 *                         Top-k sampling considers the set of topK most probable tokens.
 *                         Models running with nucleus sampling don't allow topK setting.
 *                         Note: The default value varies by model, see the Model.top_k attribute of the Model returned from the getModel function.
 *                         Empty topK field in Model indicates the model doesn't apply top-k sampling and doesn't allow setting topK on requests.
 * @see <a href="https://ai.google.dev/api/rest/v1beta/GenerationConfig">GenerationConfig</a>
 */
public record GenerationConfig(
        List<String> stopSequences,
        String responseMimeType,
        String responseSchema,
        Integer maxOutputTokens,
        Double temperature,
        Double topP,
        Integer topK
) {


}
