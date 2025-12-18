package com.converter.properties2yaml.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Properties to YAML Converter Complex Tests")
class PropertiesToYamlConverterComplexTest {

    private PropertiesToYamlConverter converter;
    private Yaml yaml;

    @BeforeEach
    void setUp() {
        converter = new PropertiesToYamlConverter();
        yaml = new Yaml();
    }

    // ==================== YAML STRUCTURE VALIDATION TESTS ====================

    @Nested
    @DisplayName("YAML Structure Validation Tests")
    class YamlStructureValidationTests {

        @Test
        @DisplayName("Should produce valid parseable YAML for simple properties")
        void shouldProduceValidParseableYamlForSimpleProperties() {
            String properties = """
                    name=TestApp
                    version=1.0.0
                    enabled=true
                    count=42
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed)
                    .containsEntry("name", "TestApp")
                    .containsEntry("version", "1.0.0")
                    .containsEntry("enabled", true)
                    .containsEntry("count", 42);
        }

        @Test
        @DisplayName("Should produce valid parseable YAML for nested properties")
        void shouldProduceValidParseableYamlForNestedProperties() {
            String properties = """
                    server.port=8080
                    server.host=localhost
                    server.ssl.enabled=true
                    server.ssl.keystore=/path/to/keystore
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("server");
            @SuppressWarnings("unchecked")
            Map<String, Object> server = (Map<String, Object>) parsed.get("server");
            assertThat(server)
                    .containsEntry("port", 8080)
                    .containsEntry("host", "localhost");

            @SuppressWarnings("unchecked")
            Map<String, Object> ssl = (Map<String, Object>) server.get("ssl");
            assertThat(ssl)
                    .containsEntry("enabled", true)
                    .containsEntry("keystore", "/path/to/keystore");
        }

        @Test
        @DisplayName("Should produce valid parseable YAML for array properties")
        void shouldProduceValidParseableYamlForArrayProperties() {
            String properties = """
                    items[0]=first
                    items[1]=second
                    items[2]=third
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("items");
            @SuppressWarnings("unchecked")
            List<Object> items = (List<Object>) parsed.get("items");
            assertThat(items).containsExactly("first", "second", "third");
        }

        @Test
        @DisplayName("Should produce valid parseable YAML for complex nested arrays")
        void shouldProduceValidParseableYamlForComplexNestedArrays() {
            String properties = """
                    users[0].name=John
                    users[0].age=30
                    users[0].roles[0]=admin
                    users[0].roles[1]=user
                    users[1].name=Jane
                    users[1].age=25
                    users[1].roles[0]=user
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("users");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> users = (List<Map<String, Object>>) parsed.get("users");

            assertThat(users).hasSize(2);
            assertThat(users.get(0))
                    .containsEntry("name", "John")
                    .containsEntry("age", 30);

            @SuppressWarnings("unchecked")
            List<String> johnRoles = (List<String>) users.get(0).get("roles");
            assertThat(johnRoles).containsExactly("admin", "user");
        }
    }

    // ==================== SPRING BOOT REALISTIC SCENARIOS ====================

    @Nested
    @DisplayName("Spring Boot Realistic Scenarios")
    class SpringBootRealisticScenariosTests {

        @Test
        @DisplayName("Should convert complete logging configuration")
        void shouldConvertCompleteLoggingConfiguration() {
            String properties = """
                    logging.level.root=INFO
                    logging.level.org.springframework=WARN
                    logging.level.org.hibernate=WARN
                    logging.level.org.hibernate.SQL=DEBUG
                    logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
                    logging.level.com.zaxxer.hikari=DEBUG
                    logging.level.com.zaxxer.hikari.HikariConfig=DEBUG
                    logging.level.org.apache.kafka=INFO
                    logging.level.org.springframework.kafka=INFO
                    logging.level.com.example.myapp=DEBUG
                    logging.level.com.example.myapp.service=TRACE
                    logging.level.com.example.myapp.repository=DEBUG
                    logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} %highlight(%-5level) [%thread] %cyan(%logger{36}) - %msg%n
                    logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n
                    logging.file.name=logs/application.log
                    logging.file.max-size=10MB
                    logging.file.max-history=30
                    logging.file.total-size-cap=1GB
                    logging.logback.rollingpolicy.clean-history-on-start=false
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("logging");
            @SuppressWarnings("unchecked")
            Map<String, Object> logging = (Map<String, Object>) parsed.get("logging");
            assertThat(logging).containsKeys("level", "pattern", "file", "logback");
        }

        @Test
        @DisplayName("Should convert complete Spring MVC configuration")
        void shouldConvertCompleteSpringMvcConfiguration() {
            String properties = """
                    spring.mvc.servlet.path=/api
                    spring.mvc.format.date=yyyy-MM-dd
                    spring.mvc.format.date-time=yyyy-MM-dd HH:mm:ss
                    spring.mvc.format.time=HH:mm:ss
                    spring.mvc.throw-exception-if-no-handler-found=true
                    spring.mvc.static-path-pattern=/static/**
                    spring.mvc.view.prefix=/WEB-INF/views/
                    spring.mvc.view.suffix=.jsp
                    spring.mvc.contentnegotiation.favor-parameter=true
                    spring.mvc.contentnegotiation.parameter-name=mediaType
                    spring.web.resources.static-locations=classpath:/static/,classpath:/public/
                    spring.web.resources.cache.period=3600
                    spring.web.resources.chain.strategy.content.enabled=true
                    spring.web.resources.chain.strategy.content.paths=/**
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("spring");
            @SuppressWarnings("unchecked")
            Map<String, Object> spring = (Map<String, Object>) parsed.get("spring");
            assertThat(spring).containsKeys("mvc", "web");
        }

        @Test
        @DisplayName("Should convert complete OpenAPI/Swagger configuration")
        void shouldConvertCompleteOpenApiSwaggerConfiguration() {
            String properties = """
                    springdoc.api-docs.enabled=true
                    springdoc.api-docs.path=/v3/api-docs
                    springdoc.swagger-ui.enabled=true
                    springdoc.swagger-ui.path=/swagger-ui.html
                    springdoc.swagger-ui.operationsSorter=method
                    springdoc.swagger-ui.tagsSorter=alpha
                    springdoc.swagger-ui.try-it-out-enabled=true
                    springdoc.swagger-ui.filter=true
                    springdoc.swagger-ui.syntax-highlight.activated=true
                    springdoc.swagger-ui.oauth.client-id=swagger-client
                    springdoc.swagger-ui.oauth.client-secret=swagger-secret
                    springdoc.packages-to-scan=com.example.controller
                    springdoc.paths-to-match=/api/**
                    springdoc.show-actuator=true
                    springdoc.default-consumes-media-type=application/json
                    springdoc.default-produces-media-type=application/json
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("springdoc");
            @SuppressWarnings("unchecked")
            Map<String, Object> springdoc = (Map<String, Object>) parsed.get("springdoc");
            assertThat(springdoc).containsKeys("api-docs", "swagger-ui", "packages-to-scan");
        }

        @Test
        @DisplayName("Should convert complete Flyway migration configuration")
        void shouldConvertCompleteFlywayMigrationConfiguration() {
            String properties = """
                    spring.flyway.enabled=true
                    spring.flyway.url=jdbc:postgresql://localhost:5432/mydb
                    spring.flyway.user=flyway_user
                    spring.flyway.password=flyway_pass
                    spring.flyway.locations=classpath:db/migration,classpath:db/seed
                    spring.flyway.schemas=public,app
                    spring.flyway.table=schema_version
                    spring.flyway.baseline-on-migrate=true
                    spring.flyway.baseline-version=1.0.0
                    spring.flyway.out-of-order=false
                    spring.flyway.validate-on-migrate=true
                    spring.flyway.clean-disabled=true
                    spring.flyway.clean-on-validation-error=false
                    spring.flyway.connect-retries=3
                    spring.flyway.connect-retries-interval=1
                    spring.flyway.placeholders.env=production
                    spring.flyway.placeholders.region=us-east-1
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("spring");
            @SuppressWarnings("unchecked")
            Map<String, Object> spring = (Map<String, Object>) parsed.get("spring");
            assertThat(spring).containsKey("flyway");
        }

        @Test
        @DisplayName("Should convert complete Quartz scheduler configuration")
        void shouldConvertCompleteQuartzSchedulerConfiguration() {
            String properties = """
                    spring.quartz.job-store-type=jdbc
                    spring.quartz.jdbc.initialize-schema=always
                    spring.quartz.jdbc.comment-prefix=--
                    spring.quartz.properties.org.quartz.scheduler.instanceName=MyScheduler
                    spring.quartz.properties.org.quartz.scheduler.instanceId=AUTO
                    spring.quartz.properties.org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
                    spring.quartz.properties.org.quartz.threadPool.threadCount=10
                    spring.quartz.properties.org.quartz.threadPool.threadPriority=5
                    spring.quartz.properties.org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
                    spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
                    spring.quartz.properties.org.quartz.jobStore.useProperties=true
                    spring.quartz.properties.org.quartz.jobStore.tablePrefix=QRTZ_
                    spring.quartz.properties.org.quartz.jobStore.isClustered=true
                    spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval=15000
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("spring");
            @SuppressWarnings("unchecked")
            Map<String, Object> spring = (Map<String, Object>) parsed.get("spring");
            assertThat(spring).containsKey("quartz");
        }
    }

    // ==================== EDGE CASES AND BOUNDARY CONDITIONS ====================

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditionsTests {

        @Test
        @DisplayName("Should handle property key with trailing dot")
        void shouldHandlePropertyKeyWithTrailingDot() {
            // This is technically invalid but should be handled gracefully
            String properties = "key.=value";

            String yamlOutput = converter.convert(properties);
            assertThat(yamlOutput).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle property key starting with dot")
        void shouldHandlePropertyKeyStartingWithDot() {
            // This is technically unusual but should be handled
            String properties = ".key=value";

            String yamlOutput = converter.convert(properties);
            assertThat(yamlOutput).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle consecutive dots in property key")
        void shouldHandleConsecutiveDotsInPropertyKey() {
            String properties = "key..nested=value";

            String yamlOutput = converter.convert(properties);
            assertThat(yamlOutput).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle numeric keys")
        void shouldHandleNumericKeys() {
            String properties = """
                    123=numericKey
                    config.456.value=nestedNumeric
                    """;

            String yamlOutput = converter.convert(properties);
            assertThat(yamlOutput).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle very long property keys")
        void shouldHandleVeryLongPropertyKeys() {
            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                if (i > 0) keyBuilder.append(".");
                keyBuilder.append("segment").append(i);
            }
            String properties = keyBuilder + "=deepValue";

            String yamlOutput = converter.convert(properties);
            assertThat(yamlOutput).contains("segment0:");
            assertThat(yamlOutput).contains("deepValue");
        }

        @Test
        @DisplayName("Should handle very long property values")
        void shouldHandleVeryLongPropertyValues() {
            String longValue = "x".repeat(10000);
            String properties = "key=" + longValue;

            String yamlOutput = converter.convert(properties);
            assertThat(yamlOutput).contains(longValue);
        }

        @Test
        @DisplayName("Should handle maximum integer boundary")
        void shouldHandleMaximumIntegerBoundary() {
            String properties = """
                    max.int=2147483647
                    min.int=-2147483648
                    overflow.int=2147483648
                    underflow.int=-2147483649
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            @SuppressWarnings("unchecked")
            Map<String, Object> max = (Map<String, Object>) parsed.get("max");
            assertThat(max.get("int")).isEqualTo(2147483647);

            @SuppressWarnings("unchecked")
            Map<String, Object> overflow = (Map<String, Object>) parsed.get("overflow");
            assertThat(overflow.get("int")).isEqualTo(2147483648L);
        }

        @Test
        @DisplayName("Should handle mixed array and non-array at same path")
        void shouldHandleMixedArrayAndNonArrayAtSamePath() {
            String properties = """
                    config.items=simpleValue
                    config.items[0]=arrayValue
                    """;

            // This is a conflict scenario - it's expected to throw an exception
            // as the same property key cannot be both a simple value and an array
            assertThatThrownBy(() -> converter.convert(properties))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle property with backslash")
        void shouldHandlePropertyWithBackslash() {
            String properties = "path=C:\\\\Users\\\\name\\\\file.txt";

            String yamlOutput = converter.convert(properties);
            assertThat(yamlOutput).contains("path:");
        }

        @Test
        @DisplayName("Should handle property with newline character in value")
        void shouldHandlePropertyWithNewlineCharacterInValue() {
            String properties = "message=line1\\nline2\\nline3";

            String yamlOutput = converter.convert(properties);
            assertThat(yamlOutput).contains("message:");
        }

        @Test
        @DisplayName("Should handle property with tab character")
        void shouldHandlePropertyWithTabCharacter() {
            String properties = "data=col1\\tcol2\\tcol3";

            String yamlOutput = converter.convert(properties);
            assertThat(yamlOutput).contains("data:");
        }
    }

    // ==================== FILE OPERATIONS TESTS ====================

    @Nested
    @DisplayName("File Operations Tests")
    class FileOperationsTests {

        @Test
        @DisplayName("Should convert file with BOM marker")
        void shouldConvertFileWithBomMarker(@TempDir Path tempDir) throws IOException {
            Path propertiesFile = tempDir.resolve("bom.properties");
            // UTF-8 BOM
            byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] content = "key=value".getBytes();
            byte[] fileContent = new byte[bom.length + content.length];
            System.arraycopy(bom, 0, fileContent, 0, bom.length);
            System.arraycopy(content, 0, fileContent, bom.length, content.length);
            Files.write(propertiesFile, fileContent);

            String yamlOutput = converter.convertFile(propertiesFile);
            assertThat(yamlOutput).isNotEmpty();
        }

        @Test
        @DisplayName("Should throw exception for non-existent file")
        void shouldThrowExceptionForNonExistentFile(@TempDir Path tempDir) {
            Path nonExistentFile = tempDir.resolve("nonexistent.properties");

            assertThatThrownBy(() -> converter.convertFile(nonExistentFile))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to read properties file");
        }

        @Test
        @DisplayName("Should convert and save to output file")
        void shouldConvertAndSaveToOutputFile(@TempDir Path tempDir) throws IOException {
            Path inputFile = tempDir.resolve("input.properties");
            Path outputFile = tempDir.resolve("output.yaml");

            String propertiesContent = """
                    spring.application.name=FileTest
                    server.port=9090
                    database.url=jdbc:h2:mem:testdb
                    """;
            Files.writeString(inputFile, propertiesContent);

            converter.convertFileToFile(inputFile, outputFile);

            assertThat(outputFile).exists();
            String yamlContent = Files.readString(outputFile);
            Map<String, Object> parsed = yaml.load(yamlContent);

            assertThat(parsed).containsKey("spring");
            assertThat(parsed).containsKey("server");
            assertThat(parsed).containsKey("database");
        }

        @Test
        @DisplayName("Should handle file with ISO-8859-1 encoding")
        void shouldHandleFileWithIso88591Encoding(@TempDir Path tempDir) throws IOException {
            Path propertiesFile = tempDir.resolve("iso.properties");
            // Properties files traditionally use ISO-8859-1
            String content = "key=value\nmessage=café";
            Files.writeString(propertiesFile, content);

            String yamlOutput = converter.convertFile(propertiesFile);
            assertThat(yamlOutput).contains("key: value");
        }

        @Test
        @DisplayName("Should handle large file with many properties")
        void shouldHandleLargeFileWithManyProperties(@TempDir Path tempDir) throws IOException {
            Path propertiesFile = tempDir.resolve("large.properties");

            StringBuilder content = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                content.append(String.format("app.module%d.setting%d=value%d%n", i / 100, i, i));
            }
            Files.writeString(propertiesFile, content.toString());

            String yamlOutput = converter.convertFile(propertiesFile);

            assertThat(yamlOutput).isNotEmpty();
            assertThat(yamlOutput.length()).isGreaterThan(100000);
        }
    }

    // ==================== PARAMETERIZED VALUE CONVERSION TESTS ====================

    @Nested
    @DisplayName("Parameterized Value Conversion Tests")
    class ParameterizedValueConversionTests {

        @ParameterizedTest
        @CsvSource({
                "true, true",
                "false, false",
                "TRUE, true",
                "FALSE, false",
                "True, true",
                "False, false"
        })
        @DisplayName("Should convert boolean values correctly")
        void shouldConvertBooleanValuesCorrectly(String input, boolean expected) {
            String properties = "flag=" + input;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed.get("flag")).isEqualTo(expected);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "0", "1", "-1", "42", "-42",
                "2147483647", "-2147483648"
        })
        @DisplayName("Should convert integer values correctly")
        void shouldConvertIntegerValuesCorrectly(String value) {
            String properties = "number=" + value;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed.get("number")).isInstanceOf(Number.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "3.14", "-3.14", "0.0", "1.0E10", "1.0e-10",
                "999999999999.999", "-0.000001"
        })
        @DisplayName("Should convert double values correctly")
        void shouldConvertDoubleValuesCorrectly(String value) {
            String properties = "decimal=" + value;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed.get("decimal")).isInstanceOf(Number.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "hello",
                "Hello World",
                "hello-world",
                "hello_world",
                "hello.world",
                "123abc",
                "abc123",
                "UPPERCASE",
                "MixedCase"
        })
        @DisplayName("Should preserve string values correctly")
        void shouldPreserveStringValuesCorrectly(String value) {
            String properties = "text=" + value;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed.get("text").toString()).isEqualTo(value);
        }
    }

    // ==================== YAML FORMATTING TESTS ====================

    @Nested
    @DisplayName("YAML Formatting Tests")
    class YamlFormattingTests {

        @Test
        @DisplayName("Should use block style for nested structures")
        void shouldUseBlockStyleForNestedStructures() {
            String properties = """
                    server.port=8080
                    server.host=localhost
                    """;

            String yamlOutput = converter.convert(properties);

            // Block style should have newlines and proper indentation
            assertThat(yamlOutput).contains("\n");
            assertThat(yamlOutput).doesNotContain("{");
            assertThat(yamlOutput).doesNotContain("}");
        }

        @Test
        @DisplayName("Should maintain consistent indentation")
        void shouldMaintainConsistentIndentation() {
            String properties = """
                    level1.level2.level3.value=test
                    level1.level2.other=value
                    """;

            String yamlOutput = converter.convert(properties);

            // Check that YAML is properly indented
            String[] lines = yamlOutput.split("\n");
            boolean foundLevel2 = false;
            boolean foundLevel3 = false;

            for (String line : lines) {
                if (line.contains("level2:")) {
                    foundLevel2 = true;
                    assertThat(line).startsWith("  ");
                }
                if (line.contains("level3:")) {
                    foundLevel3 = true;
                    assertThat(line).startsWith("    ");
                }
            }

            assertThat(foundLevel2).isTrue();
            assertThat(foundLevel3).isTrue();
        }

        @Test
        @DisplayName("Should sort keys alphabetically")
        void shouldSortKeysAlphabetically() {
            String properties = """
                    zebra=last
                    apple=first
                    mango=middle
                    """;

            String yamlOutput = converter.convert(properties);

            int appleIndex = yamlOutput.indexOf("apple:");
            int mangoIndex = yamlOutput.indexOf("mango:");
            int zebraIndex = yamlOutput.indexOf("zebra:");

            assertThat(appleIndex).isLessThan(mangoIndex);
            assertThat(mangoIndex).isLessThan(zebraIndex);
        }
    }

    // ==================== CONCURRENT CONVERSION TESTS ====================

    @Nested
    @DisplayName("Concurrent Conversion Tests")
    class ConcurrentConversionTests {

        @Test
        @DisplayName("Should handle concurrent conversions safely")
        void shouldHandleConcurrentConversionsSafely() throws InterruptedException {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            String[] results = new String[threadCount];
            Exception[] exceptions = new Exception[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        String properties = String.format("""
                                app.thread=%d
                                app.name=Thread%d
                                app.config.value=config%d
                                """, index, index, index);
                        results[index] = converter.convert(properties);
                    } catch (Exception e) {
                        exceptions[index] = e;
                    }
                });
            }

            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }

            // Wait for completion
            for (Thread thread : threads) {
                thread.join();
            }

            // Verify no exceptions occurred
            for (Exception exception : exceptions) {
                assertThat(exception).isNull();
            }

            // Verify all results are valid
            for (int i = 0; i < threadCount; i++) {
                assertThat(results[i]).isNotNull();
                assertThat(results[i]).contains("app:");
                assertThat(results[i]).contains("thread: " + i);
            }
        }
    }

    // ==================== REAL-WORLD MICROSERVICE CONFIGS ====================

    @Nested
    @DisplayName("Real-World Microservice Configuration Tests")
    class RealWorldMicroserviceConfigTests {

        @Test
        @DisplayName("Should convert complete Docker/Container configuration")
        void shouldConvertCompleteDockerContainerConfiguration() {
            String properties = """
                    # Container health checks
                    management.health.livenessstate.enabled=true
                    management.health.readinessstate.enabled=true
                    management.endpoint.health.probes.enabled=true
                    management.endpoint.health.group.liveness.include=livenessState,diskSpace
                    management.endpoint.health.group.readiness.include=readinessState,db,redis

                    # Graceful shutdown
                    server.shutdown=graceful
                    spring.lifecycle.timeout-per-shutdown-phase=30s

                    # Container-friendly logging
                    logging.pattern.console={"timestamp":"%d{yyyy-MM-dd HH:mm:ss.SSS}","level":"%level","service":"${spring.application.name}","trace_id":"%X{traceId}","span_id":"%X{spanId}","message":"%msg"}%n

                    # Resource limits awareness
                    spring.jmx.enabled=false
                    server.tomcat.threads.max=200
                    server.tomcat.accept-count=100
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKeys("management", "server", "spring", "logging");
        }

        @Test
        @DisplayName("Should convert complete observability stack configuration")
        void shouldConvertCompleteObservabilityStackConfiguration() {
            String properties = """
                    # Micrometer/Prometheus
                    management.metrics.export.prometheus.enabled=true
                    management.metrics.export.prometheus.step=1m
                    management.metrics.distribution.percentiles-histogram.http.server.requests=true
                    management.metrics.distribution.percentiles.http.server.requests=0.5,0.9,0.95,0.99
                    management.metrics.distribution.slo.http.server.requests=10ms,50ms,100ms,200ms,500ms
                    management.metrics.tags.application=${spring.application.name}
                    management.metrics.tags.environment=${spring.profiles.active}

                    # Distributed tracing (Zipkin/Jaeger)
                    spring.sleuth.enabled=true
                    spring.sleuth.sampler.probability=1.0
                    spring.zipkin.enabled=true
                    spring.zipkin.base-url=http://zipkin:9411
                    spring.zipkin.sender.type=web

                    # Logging correlation
                    logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("management");
            assertThat(parsed).containsKey("spring");
            assertThat(parsed).containsKey("logging");
        }

        @Test
        @DisplayName("Should convert complete API Gateway configuration")
        void shouldConvertCompleteApiGatewayConfiguration() {
            String properties = """
                    # Spring Cloud Gateway routes
                    spring.cloud.gateway.routes[0].id=user-service
                    spring.cloud.gateway.routes[0].uri=lb://user-service
                    spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**
                    spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1
                    spring.cloud.gateway.routes[0].filters[1]=AddRequestHeader=X-Request-Source,gateway
                    spring.cloud.gateway.routes[0].metadata.response-timeout=5000
                    spring.cloud.gateway.routes[0].metadata.connect-timeout=1000

                    spring.cloud.gateway.routes[1].id=order-service
                    spring.cloud.gateway.routes[1].uri=lb://order-service
                    spring.cloud.gateway.routes[1].predicates[0]=Path=/api/orders/**
                    spring.cloud.gateway.routes[1].predicates[1]=Method=GET,POST,PUT,DELETE
                    spring.cloud.gateway.routes[1].filters[0]=StripPrefix=1
                    spring.cloud.gateway.routes[1].filters[1]=CircuitBreaker=name=orderCircuitBreaker,fallbackUri=forward:/fallback/orders

                    spring.cloud.gateway.routes[2].id=product-service
                    spring.cloud.gateway.routes[2].uri=lb://product-service
                    spring.cloud.gateway.routes[2].predicates[0]=Path=/api/products/**
                    spring.cloud.gateway.routes[2].filters[0]=StripPrefix=1
                    spring.cloud.gateway.routes[2].filters[1]=RequestRateLimiter=redis-rate-limiter.replenishRate=10,redis-rate-limiter.burstCapacity=20

                    # Global filters
                    spring.cloud.gateway.default-filters[0]=AddResponseHeader=X-Response-Time,%{response-time}
                    spring.cloud.gateway.default-filters[1]=SaveSession

                    # Global CORS
                    spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origins=*
                    spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-methods=GET,POST,PUT,DELETE,OPTIONS
                    spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-headers=*
                    """;

            String yamlOutput = converter.convert(properties);
            Map<String, Object> parsed = yaml.load(yamlOutput);

            assertThat(parsed).containsKey("spring");
            @SuppressWarnings("unchecked")
            Map<String, Object> spring = (Map<String, Object>) parsed.get("spring");
            assertThat(spring).containsKey("cloud");
        }
    }
}
