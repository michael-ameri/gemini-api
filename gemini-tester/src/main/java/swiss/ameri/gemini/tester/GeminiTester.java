package swiss.ameri.gemini.tester;

import swiss.ameri.gemini.api.*;
import swiss.ameri.gemini.gson.GsonJsonParser;
import swiss.ameri.gemini.spi.JsonParser;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GeminiTester {

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
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
        var model = GenerativeModel.of(
                ModelVariant.GEMINI_1_0_PRO,
                List.of(
                        new Content.TextContent(
                                Content.Role.USER.roleName(),
                                "Write a 300 word story about a magic backpack."
                        )
                ),
                List.of(
                        SafetySetting.of(SafetySetting.HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
                ),
                new GenerationConfig(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );
        genAi.generateContent(model)
                .thenAccept(System.out::println)
                .get(20, TimeUnit.SECONDS);

        System.out.println("-----");
        genAi.generateContentStream(model)
                .forEach(System.out::println);
    }
}
