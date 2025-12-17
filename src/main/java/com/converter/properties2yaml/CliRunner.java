package com.converter.properties2yaml;

import com.converter.properties2yaml.service.PropertiesToYamlConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class CliRunner implements CommandLineRunner {

    private final PropertiesToYamlConverter converter;

    public CliRunner(PropertiesToYamlConverter converter) {
        this.converter = converter;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            // No arguments, run as web server (default Spring Boot behavior)
            return;
        }

        if ("--help".equals(args[0]) || "-h".equals(args[0])) {
            printHelp();
            System.exit(0);
        }

        if (args.length < 1) {
            System.err.println("Error: Please provide input file path");
            printHelp();
            System.exit(1);
        }

        Path inputPath = Paths.get(args[0]);

        if (!Files.exists(inputPath)) {
            System.err.println("Error: Input file does not exist: " + inputPath);
            System.exit(1);
        }

        Path outputPath;
        if (args.length >= 2) {
            outputPath = Paths.get(args[1]);
        } else {
            // Default output: same name but with .yaml extension
            String inputFileName = inputPath.getFileName().toString();
            String outputFileName = inputFileName.replaceAll("\\.properties$", "") + ".yaml";
            outputPath = inputPath.getParent() != null
                    ? inputPath.getParent().resolve(outputFileName)
                    : Paths.get(outputFileName);
        }

        try {
            String yamlContent = converter.convertFile(inputPath);

            if ("--stdout".equals(args.length > 1 ? args[1] : "")) {
                System.out.println(yamlContent);
            } else {
                Files.writeString(outputPath, yamlContent);
                System.out.println("Successfully converted: " + inputPath + " -> " + outputPath);
            }
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
            System.exit(1);
        }
    }

    private void printHelp() {
        System.out.println("Properties to YAML Converter");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar properties2yaml.jar                    Start web server");
        System.out.println("  java -jar properties2yaml.jar <input>            Convert file (output: input.yaml)");
        System.out.println("  java -jar properties2yaml.jar <input> <output>   Convert file to specified output");
        System.out.println("  java -jar properties2yaml.jar <input> --stdout   Convert and print to stdout");
        System.out.println("  java -jar properties2yaml.jar --help             Show this help message");
        System.out.println();
        System.out.println("Web API Endpoints:");
        System.out.println("  POST /api/convert           - JSON body with 'propertiesContent' field");
        System.out.println("  POST /api/convert/text      - Plain text properties content");
        System.out.println("  POST /api/convert/file      - Multipart file upload");
        System.out.println("  POST /api/convert/file/download - Upload and download converted YAML");
    }
}
