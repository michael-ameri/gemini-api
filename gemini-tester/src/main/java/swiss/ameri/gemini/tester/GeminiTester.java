package swiss.ameri.gemini.tester;

import swiss.ameri.gemini.api.Content;
import swiss.ameri.gemini.api.GenAi;
import swiss.ameri.gemini.api.GenerativeModel;
import swiss.ameri.gemini.api.ModelVariant;
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
        genAi.generateContent(GenerativeModel.of(ModelVariant.GEMINI_1_0_PRO, List.of(
                        new Content.TextContent(
                                Content.Role.USER.roleName(),
                                "Write long a story about a magic backpack."
                        )
                )))
                .thenAccept(System.out::println)
                .get(20, TimeUnit.SECONDS);

        System.out.println("-----");
        genAi.generateContentStream(GenerativeModel.of(ModelVariant.GEMINI_1_0_PRO, List.of(
                        new Content.TextContent(
                                Content.Role.USER.roleName(),
                                "Write long a story about a magic backpack."
                        )
                )))
                .forEach(System.out::println);
    }
}
