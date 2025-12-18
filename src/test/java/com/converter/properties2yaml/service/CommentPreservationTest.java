package com.converter.properties2yaml.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Comment Preservation Tests")
class CommentPreservationTest {

    private PropertiesToYamlConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PropertiesToYamlConverter();
    }

    @Test
    @DisplayName("Should preserve comments when flag is true")
    void shouldPreserveCommentsWhenFlagIsTrue() {
        String properties = """
                # Database configuration
                db.host=localhost
                db.port=5432
                """;

        String yaml = converter.convert(properties, true);

        assertThat(yaml).contains("# Database configuration");
        assertThat(yaml).contains("db:");
        assertThat(yaml).contains("host: localhost");
        assertThat(yaml).contains("port: 5432");
    }

    @Test
    @DisplayName("Should strip comments when flag is false")
    void shouldStripCommentsWhenFlagIsFalse() {
        String properties = """
                # Database configuration
                db.host=localhost
                db.port=5432
                """;

        String yaml = converter.convert(properties, false);

        assertThat(yaml).doesNotContain("# Database configuration");
        assertThat(yaml).contains("db:");
        assertThat(yaml).contains("host: localhost");
        assertThat(yaml).contains("port: 5432");
    }

    @Test
    @DisplayName("Should preserve inline comments")
    void shouldPreserveInlineComments() {
        String properties = """
                server.port=8080 # Production port
                """;

        String yaml = converter.convert(properties, true);

        assertThat(yaml).contains("# Production port");
        assertThat(yaml).contains("server:");
        assertThat(yaml).contains("port: 8080");
    }

    @Test
    @DisplayName("Should preserve multiple comment lines")
    void shouldPreserveMultipleCommentLines() {
        String properties = """
                # Database configuration section
                # Contains connection settings
                db.url=jdbc:postgresql://localhost/mydb
                """;

        String yaml = converter.convert(properties, true);

        assertThat(yaml).contains("# Database configuration section");
        assertThat(yaml).contains("# Contains connection settings");
    }

    @Test
    @DisplayName("Should preserve comments with exclamation mark style")
    void shouldPreserveExclamationComments() {
        String properties = """
                ! Old style comment
                key=value
                """;

        String yaml = converter.convert(properties, true);

        assertThat(yaml).contains("# Old style comment");
        assertThat(yaml).contains("key: value");
    }

    @Test
    @DisplayName("Should preserve header comments")
    void shouldPreserveHeaderComments() {
        String properties = """
                # Application configuration file
                # Version 1.0

                app.name=MyApp
                """;

        String yaml = converter.convert(properties, true);

        assertThat(yaml).contains("# Application configuration file");
        assertThat(yaml).contains("# Version 1.0");
    }

    @Test
    @DisplayName("Should preserve comments with priority sorting")
    void shouldPreserveCommentsWithPrioritySorting() {
        String properties = """
                # Custom property
                custom.value=test

                # Spring property
                spring.application.name=MyApp
                """;

        String yaml = converter.convert(properties, true);

        assertThat(yaml).contains("# Spring property");
        assertThat(yaml).contains("# Custom property");
        int springIndex = yaml.indexOf("# Spring property");
        int customIndex = yaml.indexOf("# Custom property");
        assertThat(springIndex).isLessThan(customIndex);
    }
}
