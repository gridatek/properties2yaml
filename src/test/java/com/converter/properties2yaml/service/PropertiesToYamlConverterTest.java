package com.converter.properties2yaml.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesToYamlConverterTest {

    private PropertiesToYamlConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PropertiesToYamlConverter();
    }

    @Test
    void testSimpleProperties() {
        String properties = "name=John\nage=30";
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("name: John"));
        assertTrue(yaml.contains("age: 30"));
    }

    @Test
    void testNestedProperties() {
        String properties = """
                server.port=8080
                server.host=localhost
                server.ssl.enabled=true
                """;
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("server:"));
        assertTrue(yaml.contains("port: 8080"));
        assertTrue(yaml.contains("host: localhost"));
        assertTrue(yaml.contains("ssl:"));
        assertTrue(yaml.contains("enabled: true"));
    }

    @Test
    void testBooleanValues() {
        String properties = """
                feature.enabled=true
                feature.disabled=false
                """;
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("enabled: true"));
        assertTrue(yaml.contains("disabled: false"));
    }

    @Test
    void testNumericValues() {
        String properties = """
                count=42
                price=19.99
                bigNumber=9999999999
                """;
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("count: 42"));
        assertTrue(yaml.contains("price: 19.99"));
        assertTrue(yaml.contains("bigNumber: 9999999999"));
    }

    @Test
    void testArrayProperties() {
        String properties = """
                items[0]=first
                items[1]=second
                items[2]=third
                """;
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("items:"));
        assertTrue(yaml.contains("- first"));
        assertTrue(yaml.contains("- second"));
        assertTrue(yaml.contains("- third"));
    }

    @Test
    void testComplexNestedProperties() {
        String properties = """
                spring.datasource.url=jdbc:mysql://localhost:3306/mydb
                spring.datasource.username=root
                spring.datasource.password=secret
                spring.jpa.hibernate.ddl-auto=update
                spring.jpa.show-sql=true
                """;
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("spring:"));
        assertTrue(yaml.contains("datasource:"));
        assertTrue(yaml.contains("url: jdbc:mysql://localhost:3306/mydb"));
        assertTrue(yaml.contains("jpa:"));
        assertTrue(yaml.contains("hibernate:"));
        assertTrue(yaml.contains("ddl-auto: update"));
    }

    @Test
    void testEmptyValue() {
        String properties = "empty.value=";
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("value: ''"));
    }

    @Test
    void testSpecialCharactersInValue() {
        String properties = "message=Hello, World!";
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("message: Hello, World!"));
    }

    @Test
    void testConvertFile(@TempDir Path tempDir) throws IOException {
        Path propertiesFile = tempDir.resolve("test.properties");
        Files.writeString(propertiesFile, """
                app.name=TestApp
                app.version=1.0.0
                """);

        String yaml = converter.convertFile(propertiesFile);

        assertTrue(yaml.contains("app:"));
        assertTrue(yaml.contains("name: TestApp"));
        assertTrue(yaml.contains("version: 1.0.0"));
    }

    @Test
    void testConvertFileToFile(@TempDir Path tempDir) throws IOException {
        Path propertiesFile = tempDir.resolve("test.properties");
        Path yamlFile = tempDir.resolve("test.yaml");

        Files.writeString(propertiesFile, """
                database.host=localhost
                database.port=5432
                """);

        converter.convertFileToFile(propertiesFile, yamlFile);

        assertTrue(Files.exists(yamlFile));
        String yamlContent = Files.readString(yamlFile);
        assertTrue(yamlContent.contains("database:"));
        assertTrue(yamlContent.contains("host: localhost"));
        assertTrue(yamlContent.contains("port: 5432"));
    }

    @Test
    void testPropertiesWithComments() {
        String properties = """
                # This is a comment
                name=value
                ! Another comment style
                key=data
                """;
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("name: value"));
        assertTrue(yaml.contains("key: data"));
        assertFalse(yaml.contains("comment"));
    }

    @Test
    void testUrlValue() {
        String properties = "api.endpoint=https://api.example.com/v1/users";
        String yaml = converter.convert(properties);

        assertTrue(yaml.contains("endpoint: https://api.example.com/v1/users"));
    }
}
