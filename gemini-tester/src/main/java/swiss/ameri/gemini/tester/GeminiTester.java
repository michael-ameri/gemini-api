package swiss.ameri.gemini.tester;

import swiss.ameri.gemini.api.*;
import swiss.ameri.gemini.gson.GsonJsonParser;
import swiss.ameri.gemini.spi.JsonParser;

import java.util.concurrent.TimeUnit;

/**
 * Example program to test the {@link GenAi} functionality.
 */
public class GeminiTester {

    private GeminiTester() {
        throw new AssertionError("Not instantiable");
    }

    /**
     * Entry point. takes the Gemini API key as argument. See <a href="https://aistudio.google.com/app/apikey">aistuio.google.com</a> to generate a new API key.
     *
     * @param args should receive the API key as argument
     * @throws Exception if something goes wrong
     */
    public static void main(String[] args) throws Exception {
        JsonParser parser = new GsonJsonParser();
        String apiKey = args[0];

        GenAi genAi = new GenAi(
                apiKey,
                parser
        );
        genAi.listModels()
                .forEach(System.out::println);
        System.out.println("-----");
        System.out.println(
                genAi.getModel(ModelVariant.GEMINI_1_0_PRO)
        );

        System.out.println("-----");
        var model = GenerativeModel.builder()
                .modelName(ModelVariant.GEMINI_1_0_PRO)
                .addContent(new Content.TextContent(
                        Content.Role.USER.roleName(),
                        "Write a 300 word story about a magic backpack."
                ))
                .addSafetySetting(SafetySetting.of(
                        SafetySetting.HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
                        SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH
                ))
                .generationConfig(new GenerationConfig(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ))
                .build();
        genAi.generateContent(model)
                .thenAccept(System.out::println)
                .get(20, TimeUnit.SECONDS);

        System.out.println("-----");
        genAi.generateContentStream(model)
                .forEach(System.out::println);
    }
}
