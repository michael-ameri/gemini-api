# Introduction
Java library for Gemini API. 
See the documentation here: https://ai.google.dev/gemini-api

# Getting started
- Generate an API key: https://aistudio.google.com/app/apikey

# Usage
## Dependency
Add the `gemini-api` dependency. Maven example:

        <dependency>
            <groupId>swiss.ameri</groupId>
            <artifactId>gemini-api</artifactId>
            <version>${gemini.version}</version>
        </dependency>

## JsonParser
In order for this library to stay free of dependencies, the user must provide an implementation of the `swiss.ameri.gemini.spi.JsonParser` class.
Alternatively, an example gson implementation can be used with the following dependency (which includes a gson dependency)

        <dependency>
            <groupId>swiss.ameri</groupId>
            <artifactId>gemini-gson</artifactId>
            <version>${gemini.version}</version>
        </dependency>

# Versioning
