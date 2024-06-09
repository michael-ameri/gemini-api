package swiss.ameri.gemini.tester;

import swiss.ameri.gemini.api.*;
import swiss.ameri.gemini.gson.GsonJsonParser;
import swiss.ameri.gemini.spi.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

        // each method represents an example usage
        listModels(genAi);
        getModel(genAi);
        generateContent(genAi);
        generateContentStream(genAi);
        multiChatTurn(genAi);
        textAndImage(genAi);
    }

    private static void multiChatTurn(GenAi genAi) {
        System.out.println("----- multi turn chat");
        GenerativeModel chatModel = GenerativeModel.builder()
                .modelName(ModelVariant.GEMINI_1_0_PRO)
                .addContent(new Content.TextContent(
                        Content.Role.USER.roleName(),
                        "Write the first line of a story about a magic backpack."
                ))
                .addContent(new Content.TextContent(
                        Content.Role.MODEL.roleName(),
                        "In the bustling city of Meadow brook, lived a young girl named Sophie. She was a bright and curious soul with an imaginative mind."
                ))
                .addContent(new Content.TextContent(
                        Content.Role.USER.roleName(),
                        "Can you set it in a quiet village in 1600s France? Max 30 words"
                ))
                .build();
        genAi.generateContentStream(chatModel)
                .forEach(System.out::println);
    }

    private static void generateContentStream(GenAi genAi) {
        System.out.println("----- Generate content (streaming)");
        var model = createStoryModel();
        genAi.generateContentStream(model)
                .forEach(System.out::println);
    }

    private static void generateContent(GenAi genAi) throws InterruptedException, ExecutionException, TimeoutException {
        var model = createStoryModel();
        System.out.println("----- Generate content (blocking)");
        genAi.generateContent(model)
                .thenAccept(System.out::println)
                .get(20, TimeUnit.SECONDS);
    }

    private static GenerativeModel createStoryModel() {
        return GenerativeModel.builder()
                .modelName(ModelVariant.GEMINI_1_0_PRO)
                .addContent(Content.textContent(
                        Content.Role.USER,
                        "Write a 50 word story about a magic backpack."
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
    }

    private static void getModel(GenAi genAi) {
        System.out.println("----- Get Model");
        System.out.println(
                genAi.getModel(ModelVariant.GEMINI_1_0_PRO)
        );
    }

    private static void listModels(GenAi genAi) {
        System.out.println("----- List models");
        genAi.listModels()
                .forEach(System.out::println);
    }

    private static void textAndImage(GenAi genAi) throws IOException {
        System.out.println("----- text and image");
        var model = GenerativeModel.builder()
                .modelName(ModelVariant.GEMINI_1_0_PRO_VISION)
                .addContent(
                        Content.textAndMediaContentBuilder()
                                .role(Content.Role.USER)
                                .text("What is in this image?")
                                .addMedia(new Content.MediaData(
                                        "image/png",
                                        loadSconesImage()
                                ))
                                .build()
                ).build();
        genAi.generateContent(model)
                .thenAccept(System.out::println)
                .join();
    }

    private static String loadSconesImage() throws IOException {
        try (InputStream is = GeminiTester.class.getClassLoader().getResourceAsStream("scones.png")) {
            if (is == null) {
                throw new IllegalStateException("Image not found! ");
            }
            return Base64.getEncoder().encodeToString(is.readAllBytes());
        }
    }
}
