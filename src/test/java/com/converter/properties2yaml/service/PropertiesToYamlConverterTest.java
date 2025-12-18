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

    @Test
    void testPriorityBasedSortingSpringFirst() {
        String properties = """
                custom.property=value1
                spring.application.name=TestApp
                app.name=MyApp
                server.port=8080
                zebra.config=last
                """;
        String yaml = converter.convert(properties);

        // Spring properties should come first
        int springIndex = yaml.indexOf("spring:");
        int serverIndex = yaml.indexOf("server:");
        int appIndex = yaml.indexOf("app:");
        int customIndex = yaml.indexOf("custom:");
        int zebraIndex = yaml.indexOf("zebra:");

        assertTrue(springIndex < serverIndex, "Spring properties should come before server properties");
        assertTrue(serverIndex < appIndex, "Server properties should come before app properties");
        assertTrue(appIndex < customIndex, "App properties should come before custom properties");
        assertTrue(appIndex < zebraIndex, "App properties should come before custom zebra properties");
    }

    @Test
    void testPriorityBasedSortingCommonPropertiesSecond() {
        String properties = """
                custom.value=test
                logging.level.root=INFO
                management.endpoints.web.exposure.include=health
                security.oauth2.client.registration.google.client-id=123
                """;
        String yaml = converter.convert(properties);

        // Common properties (logging, management, security) should come before custom
        int loggingIndex = yaml.indexOf("logging:");
        int managementIndex = yaml.indexOf("management:");
        int securityIndex = yaml.indexOf("security:");
        int customIndex = yaml.indexOf("custom:");

        assertTrue(loggingIndex < customIndex, "Logging properties should come before custom properties");
        assertTrue(managementIndex < customIndex, "Management properties should come before custom properties");
        assertTrue(securityIndex < customIndex, "Security properties should come before custom properties");
    }

    @Test
    void testPriorityBasedSortingApplicationPropertiesThird() {
        String properties = """
                zebra.custom=value1
                application.title=MyApp
                app.version=1.0.0
                custom.setting=test
                """;
        String yaml = converter.convert(properties);

        // App/application properties should come before custom but in order
        int appIndex = yaml.indexOf("app:");
        int applicationIndex = yaml.indexOf("application:");
        int customIndex = yaml.indexOf("custom:");
        int zebraIndex = yaml.indexOf("zebra:");

        assertTrue(appIndex < customIndex, "App properties should come before custom properties");
        assertTrue(applicationIndex < customIndex, "Application properties should come before custom properties");
        assertTrue(appIndex < zebraIndex, "App properties should come before zebra properties");
        assertTrue(applicationIndex < zebraIndex, "Application properties should come before zebra properties");
    }

    @Test
    void testAlphabeticalSortingWithinSamePriority() {
        String properties = """
                spring.jpa.hibernate.ddl-auto=update
                spring.datasource.url=jdbc:mysql://localhost/db
                spring.application.name=TestApp
                """;
        String yaml = converter.convert(properties);

        // Within spring properties, they should be alphabetically sorted
        int applicationIndex = yaml.indexOf("application:");
        int datasourceIndex = yaml.indexOf("datasource:");
        int jpaIndex = yaml.indexOf("jpa:");

        assertTrue(applicationIndex < datasourceIndex, "spring.application should come before spring.datasource");
        assertTrue(datasourceIndex < jpaIndex, "spring.datasource should come before spring.jpa");
    }

    @Test
    void testComplexPrioritySortingAllCategories() {
        String properties = """
                xyz.custom=value
                server.port=8080
                spring.application.name=TestApp
                app.version=1.0.0
                logging.level.root=INFO
                custom.property=test
                management.endpoints.enabled=true
                application.title=MyTitle
                security.user.name=admin
                """;
        String yaml = converter.convert(properties);

        // Get indices of all properties
        int springIndex = yaml.indexOf("spring:");
        int serverIndex = yaml.indexOf("server:");
        int loggingIndex = yaml.indexOf("logging:");
        int managementIndex = yaml.indexOf("management:");
        int securityIndex = yaml.indexOf("security:");
        int appIndex = yaml.indexOf("app:");
        int applicationIndex = yaml.indexOf("application:");
        int customIndex = yaml.indexOf("custom:");
        int xyzIndex = yaml.indexOf("xyz:");

        // Verify priority 1 (spring) comes first
        assertTrue(springIndex < serverIndex && springIndex < loggingIndex && springIndex < appIndex && springIndex < customIndex);

        // Verify priority 2 (server, logging, management, security) comes before priority 3 and 4
        assertTrue(serverIndex < appIndex && serverIndex < customIndex);
        assertTrue(loggingIndex < appIndex && loggingIndex < customIndex);
        assertTrue(managementIndex < appIndex && managementIndex < customIndex);
        assertTrue(securityIndex < appIndex && securityIndex < customIndex);

        // Verify priority 3 (app, application) comes before priority 4 (custom)
        assertTrue(appIndex < customIndex && appIndex < xyzIndex);
        assertTrue(applicationIndex < customIndex && applicationIndex < xyzIndex);
    }

    @Test
    void testSortingPreservesNestedStructure() {
        String properties = """
                custom.nested.deep.value=test
                spring.datasource.url=jdbc:mysql://localhost/db
                spring.datasource.username=user
                app.config.setting=value
                """;
        String yaml = converter.convert(properties);

        // Verify spring comes first
        int springIndex = yaml.indexOf("spring:");
        int appIndex = yaml.indexOf("app:");
        int customIndex = yaml.indexOf("custom:");

        assertTrue(springIndex < appIndex);
        assertTrue(appIndex < customIndex);

        // Verify nested structure is preserved
        assertTrue(yaml.contains("datasource:"));
        assertTrue(yaml.contains("url:"));
        assertTrue(yaml.contains("username:"));
        assertTrue(yaml.contains("nested:"));
        assertTrue(yaml.contains("deep:"));
    }
}
