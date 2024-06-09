# Introduction

Java library for Gemini API.
See the documentation here: https://ai.google.dev/gemini-api

# Getting started

- Generate an API key: https://aistudio.google.com/app/apikey

# Usage

## Dependency

Add the `gemini-api` dependency. Maven example (replace ${gemini.version} with the latest version):

        <dependency>
            <groupId>swiss.ameri</groupId>
            <artifactId>gemini-api</artifactId>
            <version>${gemini.version}</version>
        </dependency>

## JsonParser

In order for this library to stay free of dependencies, the user must provide an implementation of
the `swiss.ameri.gemini.spi.JsonParser` interface.
Alternatively, an example Gson implementation can be used with the following dependency (which includes a Gson
dependency)

        <dependency>
            <groupId>swiss.ameri</groupId>
            <artifactId>gemini-gson</artifactId>
            <version>${gemini.version}</version>
        </dependency>

## Example code

See [gemini-tester](https://github.com/michael-ameri/gemini-api/blob/1beta.0.1.0/gemini-tester/src/main/java/swiss/ameri/gemini/tester/GeminiTester.java)
for some examples.

    JsonParser parser = new GsonJsonParser(); // or some custom implementation
    String apiKey = ...;
    GenAi genAi = new GenAi(
                apiKey,
                parser
        );
        
    // list available models
    genAi.listModels().forEach(System.out::println);
    
    // create a prompt
    var model = GenerativeModel.builder()
        .modelName(ModelVariant.GEMINI_1_0_PRO)
        .addContent(new Content.TextContent(
                Content.Role.USER.roleName(),
                "Write a 300 word story about a magic backpack."
        ))
        .build();
    
    // execute the prompt, wait for the full response
    genAi.generateContent(model)
                .thenAccept(System.out::println)
                .get(20, TimeUnit.SECONDS); // block here until the response arrives. Probably not a good idea in production code.

    // execute the prompt, process the chunks of responses as they arrive
    genAi.generateContentStream(model)
                .forEach(System.out::println)

# Versioning

The library versioning follows the scheme:
`<gemini-api-version>.<major>.<minor>.<patch>`

Example:
`1beta.0.0.1`

# Requirements

- \>= Java 17

# Modules

The project is composed of the following maven modules, which are deployed to maven central.

## gemini-api

        <dependency>
            <groupId>swiss.ameri</groupId>
            <artifactId>gemini-api</artifactId>
            <version>${gemini.version}</version>
        </dependency>

Main module to be used. Must not contain any dependencies to other modules.

## gemini-gson

        <dependency>
            <groupId>swiss.ameri</groupId>
            <artifactId>gemini-gson</artifactId>
            <version>${gemini.version}</version>
        </dependency>

Provides an example implementation of the `swiss.ameri.gemini.spi.JsonParser` class using `Gson`.
Contains a maven dependency to `Gson`

## gemini-tester

        <dependency>
            <groupId>swiss.ameri</groupId>
            <artifactId>gemini-tester</artifactId>
            <version>${gemini.version}</version>
        </dependency>

Contains some example code of how the API can be used.
