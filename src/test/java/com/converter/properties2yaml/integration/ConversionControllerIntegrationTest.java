package com.converter.properties2yaml.integration;

import com.converter.properties2yaml.model.ConversionRequest;
import com.converter.properties2yaml.model.ConversionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Properties to YAML Conversion Integration Tests")
class ConversionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== NESTED PROPERTIES TESTS ====================

    @Nested
    @DisplayName("Deeply Nested Properties Tests")
    class DeeplyNestedPropertiesTests {

        @Test
        @DisplayName("Should convert 5-level deep nested properties")
        void shouldConvertFiveLevelDeepNestedProperties() throws Exception {
            String properties = """
                    level1.level2.level3.level4.level5.value=deepValue
                    level1.level2.level3.level4.level5.another=anotherDeep
                    level1.level2.level3.level4.siblingValue=siblingAtLevel4
                    level1.level2.level3.value=level3Value
                    level1.level2.value=level2Value
                    level1.value=level1Value
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent())
                    .contains("level1:")
                    .contains("level2:")
                    .contains("level3:")
                    .contains("level4:")
                    .contains("level5:")
                    .contains("value: deepValue")
                    .contains("another: anotherDeep")
                    .contains("siblingValue: siblingAtLevel4");
        }

        @Test
        @DisplayName("Should convert 10-level deep nested properties")
        void shouldConvertTenLevelDeepNestedProperties() throws Exception {
            String properties = """
                    a.b.c.d.e.f.g.h.i.j.finalValue=veryDeepValue
                    a.b.c.d.e.f.g.h.i.j.count=42
                    a.b.c.d.e.f.g.h.i.j.enabled=true
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent())
                    .contains("finalValue: veryDeepValue")
                    .contains("count: 42")
                    .contains("enabled: true");
        }

        @Test
        @DisplayName("Should handle complex branching nested structure")
        void shouldHandleComplexBranchingNestedStructure() throws Exception {
            String properties = """
                    app.database.primary.host=localhost
                    app.database.primary.port=5432
                    app.database.primary.credentials.username=admin
                    app.database.primary.credentials.password=secret
                    app.database.primary.pool.minSize=5
                    app.database.primary.pool.maxSize=20
                    app.database.replica.host=replica.example.com
                    app.database.replica.port=5432
                    app.database.replica.credentials.username=reader
                    app.database.replica.credentials.password=readerpass
                    app.cache.redis.host=redis.example.com
                    app.cache.redis.port=6379
                    app.cache.redis.cluster.enabled=true
                    app.cache.redis.cluster.nodes=3
                    app.messaging.kafka.bootstrap.servers=kafka1:9092,kafka2:9092
                    app.messaging.kafka.consumer.groupId=my-group
                    app.messaging.kafka.producer.acks=all
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("app:")
                    .contains("database:")
                    .contains("primary:")
                    .contains("replica:")
                    .contains("credentials:")
                    .contains("pool:")
                    .contains("cache:")
                    .contains("redis:")
                    .contains("cluster:")
                    .contains("messaging:")
                    .contains("kafka:")
                    .contains("bootstrap:")
                    .contains("consumer:")
                    .contains("producer:");
        }
    }

    // ==================== ARRAY PROPERTIES TESTS ====================

    @Nested
    @DisplayName("Array Properties Tests")
    class ArrayPropertiesTests {

        @Test
        @DisplayName("Should convert simple indexed array properties")
        void shouldConvertSimpleIndexedArrayProperties() throws Exception {
            String properties = """
                    servers[0]=server1.example.com
                    servers[1]=server2.example.com
                    servers[2]=server3.example.com
                    servers[3]=server4.example.com
                    servers[4]=server5.example.com
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent())
                    .contains("servers:")
                    .contains("- server1.example.com")
                    .contains("- server2.example.com")
                    .contains("- server3.example.com");
        }

        @Test
        @DisplayName("Should convert nested array with object properties")
        void shouldConvertNestedArrayWithObjectProperties() throws Exception {
            String properties = """
                    users[0].name=John Doe
                    users[0].email=john@example.com
                    users[0].age=30
                    users[0].active=true
                    users[1].name=Jane Smith
                    users[1].email=jane@example.com
                    users[1].age=25
                    users[1].active=false
                    users[2].name=Bob Wilson
                    users[2].email=bob@example.com
                    users[2].age=35
                    users[2].active=true
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("users:")
                    .contains("name: John Doe")
                    .contains("email: john@example.com")
                    .contains("age: 30")
                    .contains("active: true")
                    .contains("name: Jane Smith");
        }

        @Test
        @DisplayName("Should convert deeply nested arrays")
        void shouldConvertDeeplyNestedArrays() throws Exception {
            String properties = """
                    config.environments[0].name=development
                    config.environments[0].servers[0].host=dev1.example.com
                    config.environments[0].servers[0].port=8080
                    config.environments[0].servers[1].host=dev2.example.com
                    config.environments[0].servers[1].port=8081
                    config.environments[1].name=production
                    config.environments[1].servers[0].host=prod1.example.com
                    config.environments[1].servers[0].port=80
                    config.environments[1].servers[1].host=prod2.example.com
                    config.environments[1].servers[1].port=80
                    config.environments[1].servers[2].host=prod3.example.com
                    config.environments[1].servers[2].port=80
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("config:")
                    .contains("environments:")
                    .contains("name: development")
                    .contains("name: production")
                    .contains("servers:")
                    .contains("host: dev1.example.com")
                    .contains("host: prod1.example.com");
        }

        @Test
        @DisplayName("Should handle sparse array indices")
        void shouldHandleSparseArrayIndices() throws Exception {
            String properties = """
                    items[0]=first
                    items[2]=third
                    items[5]=sixth
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent()).contains("items:");
        }

        @Test
        @DisplayName("Should convert multiple arrays at same level")
        void shouldConvertMultipleArraysAtSameLevel() throws Exception {
            String properties = """
                    config.hosts[0]=host1.com
                    config.hosts[1]=host2.com
                    config.ports[0]=8080
                    config.ports[1]=8081
                    config.ports[2]=8082
                    config.protocols[0]=http
                    config.protocols[1]=https
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("hosts:")
                    .contains("ports:")
                    .contains("protocols:")
                    .contains("- host1.com")
                    .contains("- 8080")
                    .contains("- http");
        }
    }

    // ==================== SPRING BOOT PROPERTIES TESTS ====================

    @Nested
    @DisplayName("Spring Boot Specific Properties Tests")
    class SpringBootPropertiesTests {

        @Test
        @DisplayName("Should convert complete Spring Boot datasource configuration")
        void shouldConvertCompleteSpringBootDatasourceConfig() throws Exception {
            String properties = """
                    spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
                    spring.datasource.username=postgres
                    spring.datasource.password=secretPassword123!
                    spring.datasource.driver-class-name=org.postgresql.Driver
                    spring.datasource.hikari.minimum-idle=5
                    spring.datasource.hikari.maximum-pool-size=20
                    spring.datasource.hikari.idle-timeout=300000
                    spring.datasource.hikari.max-lifetime=1200000
                    spring.datasource.hikari.connection-timeout=20000
                    spring.datasource.hikari.pool-name=MyHikariPool
                    spring.datasource.hikari.auto-commit=true
                    spring.datasource.hikari.data-source-properties.cachePrepStmts=true
                    spring.datasource.hikari.data-source-properties.prepStmtCacheSize=250
                    spring.datasource.hikari.data-source-properties.prepStmtCacheSqlLimit=2048
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("spring:")
                    .contains("datasource:")
                    .contains("url: jdbc:postgresql://localhost:5432/mydb")
                    .contains("hikari:")
                    .contains("minimum-idle: 5")
                    .contains("maximum-pool-size: 20")
                    .contains("data-source-properties:");
        }

        @Test
        @DisplayName("Should convert Spring Boot JPA and Hibernate configuration")
        void shouldConvertSpringBootJpaHibernateConfig() throws Exception {
            String properties = """
                    spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
                    spring.jpa.hibernate.ddl-auto=validate
                    spring.jpa.show-sql=true
                    spring.jpa.open-in-view=false
                    spring.jpa.properties.hibernate.format_sql=true
                    spring.jpa.properties.hibernate.use_sql_comments=true
                    spring.jpa.properties.hibernate.jdbc.batch_size=50
                    spring.jpa.properties.hibernate.order_inserts=true
                    spring.jpa.properties.hibernate.order_updates=true
                    spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
                    spring.jpa.properties.hibernate.generate_statistics=true
                    spring.jpa.properties.hibernate.cache.use_second_level_cache=true
                    spring.jpa.properties.hibernate.cache.use_query_cache=true
                    spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.ehcache.EhCacheRegionFactory
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("spring:")
                    .contains("jpa:")
                    .contains("hibernate:")
                    .contains("ddl-auto: validate")
                    .contains("show-sql: true")
                    .contains("properties:")
                    .contains("jdbc:")
                    .contains("batch_size: 50")
                    .contains("cache:");
        }

        @Test
        @DisplayName("Should convert Spring Security configuration")
        void shouldConvertSpringSecurityConfig() throws Exception {
            String properties = """
                    spring.security.user.name=admin
                    spring.security.user.password=adminPassword
                    spring.security.user.roles=ADMIN,USER
                    spring.security.oauth2.client.registration.google.client-id=google-client-id
                    spring.security.oauth2.client.registration.google.client-secret=google-secret
                    spring.security.oauth2.client.registration.google.scope=openid,profile,email
                    spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
                    spring.security.oauth2.client.provider.google.token-uri=https://www.googleapis.com/oauth2/v4/token
                    spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer.example.com
                    spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://issuer.example.com/.well-known/jwks.json
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("spring:")
                    .contains("security:")
                    .contains("user:")
                    .contains("oauth2:")
                    .contains("client:")
                    .contains("registration:")
                    .contains("google:")
                    .contains("provider:")
                    .contains("resourceserver:")
                    .contains("jwt:");
        }

        @Test
        @DisplayName("Should convert Spring Cloud configuration")
        void shouldConvertSpringCloudConfig() throws Exception {
            String properties = """
                    spring.cloud.config.uri=http://config-server:8888
                    spring.cloud.config.fail-fast=true
                    spring.cloud.config.retry.initial-interval=1000
                    spring.cloud.config.retry.max-attempts=6
                    spring.cloud.config.retry.max-interval=2000
                    spring.cloud.config.retry.multiplier=1.1
                    eureka.client.service-url.defaultZone=http://eureka:8761/eureka/
                    eureka.client.register-with-eureka=true
                    eureka.client.fetch-registry=true
                    eureka.instance.prefer-ip-address=true
                    eureka.instance.lease-renewal-interval-in-seconds=30
                    eureka.instance.lease-expiration-duration-in-seconds=90
                    spring.cloud.gateway.routes[0].id=user-service
                    spring.cloud.gateway.routes[0].uri=lb://user-service
                    spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**
                    spring.cloud.gateway.routes[1].id=order-service
                    spring.cloud.gateway.routes[1].uri=lb://order-service
                    spring.cloud.gateway.routes[1].predicates[0]=Path=/api/orders/**
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("spring:")
                    .contains("cloud:")
                    .contains("config:")
                    .contains("retry:")
                    .contains("eureka:")
                    .contains("client:")
                    .contains("instance:")
                    .contains("gateway:")
                    .contains("routes:");
        }

        @Test
        @DisplayName("Should convert Spring Kafka configuration")
        void shouldConvertSpringKafkaConfig() throws Exception {
            String properties = """
                    spring.kafka.bootstrap-servers=kafka1:9092,kafka2:9092,kafka3:9092
                    spring.kafka.consumer.group-id=my-consumer-group
                    spring.kafka.consumer.auto-offset-reset=earliest
                    spring.kafka.consumer.enable-auto-commit=false
                    spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
                    spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
                    spring.kafka.consumer.max-poll-records=500
                    spring.kafka.consumer.properties.spring.json.trusted.packages=*
                    spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
                    spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
                    spring.kafka.producer.acks=all
                    spring.kafka.producer.retries=3
                    spring.kafka.producer.batch-size=16384
                    spring.kafka.producer.buffer-memory=33554432
                    spring.kafka.listener.concurrency=3
                    spring.kafka.listener.ack-mode=manual_immediate
                    spring.kafka.listener.poll-timeout=3000
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("spring:")
                    .contains("kafka:")
                    .contains("bootstrap-servers: kafka1:9092,kafka2:9092,kafka3:9092")
                    .contains("consumer:")
                    .contains("producer:")
                    .contains("listener:")
                    .contains("acks: all");
        }

        @Test
        @DisplayName("Should convert Spring Actuator configuration")
        void shouldConvertSpringActuatorConfig() throws Exception {
            String properties = """
                    management.endpoints.web.exposure.include=health,info,metrics,prometheus
                    management.endpoints.web.base-path=/actuator
                    management.endpoint.health.show-details=always
                    management.endpoint.health.show-components=always
                    management.endpoint.health.probes.enabled=true
                    management.health.diskspace.enabled=true
                    management.health.diskspace.threshold=10485760
                    management.health.db.enabled=true
                    management.health.redis.enabled=true
                    management.metrics.export.prometheus.enabled=true
                    management.metrics.distribution.percentiles-histogram.http.server.requests=true
                    management.metrics.distribution.slo.http.server.requests=50ms,100ms,200ms,400ms
                    management.metrics.tags.application=my-app
                    management.metrics.tags.environment=production
                    management.info.env.enabled=true
                    management.info.git.mode=full
                    management.info.java.enabled=true
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("management:")
                    .contains("endpoints:")
                    .contains("web:")
                    .contains("endpoint:")
                    .contains("health:")
                    .contains("metrics:")
                    .contains("info:");
        }
    }

    // ==================== DATA TYPE CONVERSION TESTS ====================

    @Nested
    @DisplayName("Data Type Conversion Tests")
    class DataTypeConversionTests {

        @Test
        @DisplayName("Should correctly convert all numeric types")
        void shouldCorrectlyConvertAllNumericTypes() throws Exception {
            String properties = """
                    integer.positive=42
                    integer.negative=-100
                    integer.zero=0
                    integer.max=2147483647
                    integer.min=-2147483648
                    long.positive=9999999999
                    long.negative=-9999999999
                    long.max=9223372036854775807
                    double.positive=3.14159
                    double.negative=-2.71828
                    double.scientific=1.23E10
                    double.small=0.000001
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("positive: 42")
                    .contains("negative: -100")
                    .contains("zero: 0")
                    .contains("max: 2147483647")
                    .contains("min: -2147483648")
                    .contains("3.14159")
                    .contains("-2.71828");
        }

        @Test
        @DisplayName("Should correctly convert boolean values")
        void shouldCorrectlyConvertBooleanValues() throws Exception {
            String properties = """
                    boolean.true.lowercase=true
                    boolean.false.lowercase=false
                    boolean.true.uppercase=TRUE
                    boolean.false.uppercase=FALSE
                    boolean.true.mixed=True
                    boolean.false.mixed=False
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            // All boolean variations should be converted to lowercase true/false
            assertThat(yaml).contains("lowercase: true");
            assertThat(yaml).contains("lowercase: false");
        }

        @Test
        @DisplayName("Should preserve string values that look like numbers but aren't")
        void shouldPreserveStringValuesThatLookLikeNumbers() throws Exception {
            String properties = """
                    version=1.0.0
                    phone=+1-555-123-4567
                    zipcode=01234
                    productCode=ABC123
                    ipAddress=192.168.1.1
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("version: 1.0.0")
                    .contains("phone: +1-555-123-4567")
                    .contains("productCode: ABC123")
                    .contains("ipAddress: 192.168.1.1");
        }
    }

    // ==================== SPECIAL CHARACTERS & EDGE CASES TESTS ====================

    @Nested
    @DisplayName("Special Characters and Edge Cases Tests")
    class SpecialCharactersAndEdgeCasesTests {

        @Test
        @DisplayName("Should handle URLs with special characters")
        void shouldHandleUrlsWithSpecialCharacters() throws Exception {
            String properties = """
                    url.simple=https://example.com
                    url.withPort=https://example.com:8443
                    url.withPath=https://example.com/api/v1/users
                    url.withQuery=https://example.com/search?q=hello&limit=10
                    url.withFragment=https://example.com/page#section
                    url.withAuth=https://user:pass@example.com/resource
                    url.complex=https://api.example.com:8443/v2/data?filter=active&sort=name&page=1#results
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("simple: https://example.com")
                    .contains("withPort: https://example.com:8443")
                    .contains("withQuery: https://example.com/search?q=hello&limit=10");
        }

        @Test
        @DisplayName("Should handle empty and whitespace values")
        void shouldHandleEmptyAndWhitespaceValues() throws Exception {
            String properties = """
                    empty.value=
                    whitespace.only=   \s
                    normal.value=normalValue
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent()).contains("normal:");
        }

        @Test
        @DisplayName("Should handle values with equals sign")
        void shouldHandleValuesWithEqualsSign() throws Exception {
            String properties = """
                    equation=a=b+c
                    connectionString=Server=localhost;Database=mydb;User=admin
                    base64=SGVsbG8gV29ybGQ=
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml).contains("equation:");
            assertThat(yaml).contains("connectionString:");
            assertThat(yaml).contains("base64:");
        }

        @Test
        @DisplayName("Should handle special YAML characters in values")
        void shouldHandleSpecialYamlCharactersInValues() throws Exception {
            String properties = """
                    colon.value=key:value
                    hash.value=color #comment
                    bracket.value=[1,2,3]
                    brace.value={key: value}
                    quote.single=it's working
                    quote.double=say "hello"
                    ampersand=rock & roll
                    asterisk=*.txt
                    pipe.value=cmd | grep
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getYamlContent()).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle Unicode characters")
        void shouldHandleUnicodeCharacters() throws Exception {
            String properties = """
                    greeting.english=Hello
                    greeting.spanish=Hola
                    greeting.chinese=你好
                    greeting.japanese=こんにちは
                    greeting.arabic=مرحبا
                    greeting.russian=Привет
                    greeting.emoji=Hello 👋 World 🌍
                    currency.euro=€100
                    currency.pound=£50
                    currency.yen=¥1000
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("english: Hello")
                    .contains("spanish: Hola");
        }

        @Test
        @DisplayName("Should handle multiline values")
        void shouldHandleMultilineValues() throws Exception {
            String properties = """
                    single.line=This is a single line
                    description=This is a long description that might span multiple lines in the original format
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should handle keys with hyphens and underscores")
        void shouldHandleKeysWithHyphensAndUnderscores() throws Exception {
            String properties = """
                    my-property-with-hyphens=value1
                    my_property_with_underscores=value2
                    mixed-property_name=value3
                    nested.hyphen-key.underscore_key.value=nestedValue
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("my-property-with-hyphens: value1")
                    .contains("my_property_with_underscores: value2")
                    .contains("mixed-property_name: value3");
        }
    }

    // ==================== FILE UPLOAD TESTS ====================

    @Nested
    @DisplayName("File Upload Integration Tests")
    class FileUploadIntegrationTests {

        @Test
        @DisplayName("Should convert uploaded properties file")
        void shouldConvertUploadedPropertiesFile() throws Exception {
            String propertiesContent = """
                    server.port=8080
                    server.host=localhost
                    database.url=jdbc:mysql://localhost:3306/mydb
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "application.properties",
                    MediaType.TEXT_PLAIN_VALUE,
                    propertiesContent.getBytes(StandardCharsets.UTF_8)
            );

            MvcResult result = mockMvc.perform(multipart("/api/convert/file")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent())
                    .contains("server:")
                    .contains("port: 8080")
                    .contains("database:");
        }

        @Test
        @DisplayName("Should convert large properties file")
        void shouldConvertLargePropertiesFile() throws Exception {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                propertiesBuilder.append(String.format("property.level1.level2.item%d=value%d%n", i, i));
            }

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "large.properties",
                    MediaType.TEXT_PLAIN_VALUE,
                    propertiesBuilder.toString().getBytes(StandardCharsets.UTF_8)
            );

            MvcResult result = mockMvc.perform(multipart("/api/convert/file")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent())
                    .contains("property:")
                    .contains("level1:")
                    .contains("level2:")
                    .contains("item0: value0")
                    .contains("item999: value999");
        }

        @Test
        @DisplayName("Should download converted YAML file with correct filename")
        void shouldDownloadConvertedYamlFileWithCorrectFilename() throws Exception {
            String propertiesContent = """
                    app.name=MyApp
                    app.version=1.0.0
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "config.properties",
                    MediaType.TEXT_PLAIN_VALUE,
                    propertiesContent.getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(multipart("/api/convert/file/download")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"config.yaml\""))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("app:")));
        }

        @Test
        @DisplayName("Should handle file with complex Spring Boot configuration")
        void shouldHandleFileWithComplexSpringBootConfiguration() throws Exception {
            String propertiesContent = """
                    # Application Configuration
                    spring.application.name=complex-app
                    server.port=8443
                    server.ssl.enabled=true
                    server.ssl.key-store=classpath:keystore.p12
                    server.ssl.key-store-password=changeit

                    # Database
                    spring.datasource.url=jdbc:postgresql://db.example.com:5432/production
                    spring.datasource.hikari.maximum-pool-size=50

                    # Security
                    spring.security.oauth2.resourceserver.jwt.issuer-uri=https://auth.example.com

                    # Kafka
                    spring.kafka.bootstrap-servers=kafka1:9092,kafka2:9092
                    spring.kafka.consumer.group-id=my-group

                    # Actuator
                    management.endpoints.web.exposure.include=health,prometheus
                    management.endpoint.health.probes.enabled=true
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "application-production.properties",
                    MediaType.TEXT_PLAIN_VALUE,
                    propertiesContent.getBytes(StandardCharsets.UTF_8)
            );

            MvcResult result = mockMvc.perform(multipart("/api/convert/file")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("spring:")
                    .contains("application:")
                    .contains("datasource:")
                    .contains("security:")
                    .contains("kafka:")
                    .contains("management:");
        }
    }

    // ==================== TEXT ENDPOINT TESTS ====================

    @Nested
    @DisplayName("Plain Text Endpoint Tests")
    class PlainTextEndpointTests {

        @Test
        @DisplayName("Should convert plain text properties to YAML")
        void shouldConvertPlainTextPropertiesToYaml() throws Exception {
            String propertiesContent = """
                    app.name=TestApp
                    app.version=2.0.0
                    feature.enabled=true
                    """;

            mockMvc.perform(post("/api/convert/text")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(propertiesContent))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("app:")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("name: TestApp")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("version: 2.0.0")));
        }

        @Test
        @DisplayName("Should handle complex properties via text endpoint")
        void shouldHandleComplexPropertiesViaTextEndpoint() throws Exception {
            String propertiesContent = """
                    database.primary.host=primary.db.example.com
                    database.primary.port=5432
                    database.replica[0].host=replica1.db.example.com
                    database.replica[0].port=5432
                    database.replica[1].host=replica2.db.example.com
                    database.replica[1].port=5432
                    """;

            mockMvc.perform(post("/api/convert/text")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(propertiesContent))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("database:")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("primary:")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("replica:")));
        }
    }

    // ==================== PARAMETERIZED TESTS ====================

    @Nested
    @DisplayName("Parameterized Conversion Tests")
    class ParameterizedConversionTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "simple.key=simpleValue",
                "nested.level1.level2=nestedValue",
                "array[0]=arrayValue",
                "mixed.array[0].key=mixedValue"
        })
        @DisplayName("Should convert various property formats")
        void shouldConvertVariousPropertyFormats(String property) throws Exception {
            ConversionRequest request = new ConversionRequest(property);

            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.yamlContent").isNotEmpty());
        }

        @ParameterizedTest
        @MethodSource("provideSpringProfiles")
        @DisplayName("Should convert Spring profile specific properties")
        void shouldConvertSpringProfileSpecificProperties(String profileName, String properties) throws Exception {
            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent()).isNotEmpty();
        }

        static Stream<Arguments> provideSpringProfiles() {
            return Stream.of(
                    Arguments.of("development", """
                            spring.profiles.active=dev
                            logging.level.root=DEBUG
                            spring.datasource.url=jdbc:h2:mem:testdb
                            """),
                    Arguments.of("production", """
                            spring.profiles.active=prod
                            logging.level.root=WARN
                            spring.datasource.url=jdbc:postgresql://prod-db:5432/app
                            server.ssl.enabled=true
                            """),
                    Arguments.of("testing", """
                            spring.profiles.active=test
                            logging.level.root=INFO
                            spring.datasource.url=jdbc:h2:mem:testdb
                            spring.jpa.hibernate.ddl-auto=create-drop
                            """)
            );
        }

        @ParameterizedTest
        @MethodSource("provideDatabaseConfigurations")
        @DisplayName("Should convert various database configurations")
        void shouldConvertVariousDatabaseConfigurations(String dbType, String properties) throws Exception {
            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent())
                    .contains("spring:")
                    .contains("datasource:");
        }

        static Stream<Arguments> provideDatabaseConfigurations() {
            return Stream.of(
                    Arguments.of("PostgreSQL", """
                            spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
                            spring.datasource.driver-class-name=org.postgresql.Driver
                            spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
                            """),
                    Arguments.of("MySQL", """
                            spring.datasource.url=jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC
                            spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
                            spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
                            """),
                    Arguments.of("Oracle", """
                            spring.datasource.url=jdbc:oracle:thin:@localhost:1521:orcl
                            spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
                            spring.jpa.database-platform=org.hibernate.dialect.Oracle12cDialect
                            """),
                    Arguments.of("SQL Server", """
                            spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=mydb
                            spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
                            spring.jpa.database-platform=org.hibernate.dialect.SQLServer2012Dialect
                            """),
                    Arguments.of("H2", """
                            spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
                            spring.datasource.driver-class-name=org.h2.Driver
                            spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
                            spring.h2.console.enabled=true
                            """)
            );
        }
    }

    // ==================== REAL-WORLD CONFIGURATION TESTS ====================

    @Nested
    @DisplayName("Real-World Configuration Tests")
    class RealWorldConfigurationTests {

        @Test
        @DisplayName("Should convert complete microservice configuration")
        void shouldConvertCompleteMicroserviceConfiguration() throws Exception {
            String properties = """
                    # Server Configuration
                    server.port=8080
                    server.servlet.context-path=/api
                    server.compression.enabled=true
                    server.compression.mime-types=application/json,application/xml,text/html
                    server.compression.min-response-size=1024

                    # Application Info
                    spring.application.name=order-service
                    spring.profiles.active=production

                    # Database Configuration
                    spring.datasource.url=jdbc:postgresql://db.example.com:5432/orders
                    spring.datasource.username=${DB_USERNAME}
                    spring.datasource.password=${DB_PASSWORD}
                    spring.datasource.hikari.minimum-idle=10
                    spring.datasource.hikari.maximum-pool-size=50
                    spring.datasource.hikari.idle-timeout=600000

                    # JPA Configuration
                    spring.jpa.hibernate.ddl-auto=validate
                    spring.jpa.show-sql=false
                    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
                    spring.jpa.properties.hibernate.jdbc.batch_size=50

                    # Redis Cache
                    spring.cache.type=redis
                    spring.redis.host=redis.example.com
                    spring.redis.port=6379
                    spring.redis.password=${REDIS_PASSWORD}
                    spring.redis.lettuce.pool.max-active=16
                    spring.redis.lettuce.pool.max-idle=8

                    # Kafka Configuration
                    spring.kafka.bootstrap-servers=kafka1:9092,kafka2:9092,kafka3:9092
                    spring.kafka.consumer.group-id=order-service
                    spring.kafka.consumer.auto-offset-reset=earliest
                    spring.kafka.producer.acks=all
                    spring.kafka.producer.retries=3

                    # Eureka Service Discovery
                    eureka.client.service-url.defaultZone=http://eureka1:8761/eureka,http://eureka2:8761/eureka
                    eureka.client.register-with-eureka=true
                    eureka.client.fetch-registry=true
                    eureka.instance.prefer-ip-address=true
                    eureka.instance.instance-id=${spring.application.name}:${random.uuid}

                    # Resilience4j Circuit Breaker
                    resilience4j.circuitbreaker.instances.inventoryService.register-health-indicator=true
                    resilience4j.circuitbreaker.instances.inventoryService.sliding-window-size=10
                    resilience4j.circuitbreaker.instances.inventoryService.failure-rate-threshold=50
                    resilience4j.circuitbreaker.instances.inventoryService.wait-duration-in-open-state=10000
                    resilience4j.circuitbreaker.instances.paymentService.register-health-indicator=true
                    resilience4j.circuitbreaker.instances.paymentService.sliding-window-size=5
                    resilience4j.circuitbreaker.instances.paymentService.failure-rate-threshold=60

                    # Actuator
                    management.endpoints.web.exposure.include=health,info,metrics,prometheus
                    management.endpoint.health.show-details=always
                    management.metrics.export.prometheus.enabled=true
                    management.metrics.tags.application=${spring.application.name}

                    # Logging
                    logging.level.root=INFO
                    logging.level.com.example.orderservice=DEBUG
                    logging.level.org.springframework.web=WARN
                    logging.level.org.hibernate.SQL=DEBUG
                    logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

                    # Custom Application Properties
                    app.order.expiration-minutes=30
                    app.order.max-items=100
                    app.notification.email.enabled=true
                    app.notification.sms.enabled=false
                    app.retry.max-attempts=3
                    app.retry.backoff-delay=1000
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();

            // Verify major sections are present
            assertThat(yaml)
                    .contains("server:")
                    .contains("spring:")
                    .contains("eureka:")
                    .contains("resilience4j:")
                    .contains("management:")
                    .contains("logging:")
                    .contains("app:");

            // Verify nested structures
            assertThat(yaml)
                    .contains("datasource:")
                    .contains("hikari:")
                    .contains("kafka:")
                    .contains("redis:")
                    .contains("circuitbreaker:")
                    .contains("instances:");
        }

        @Test
        @DisplayName("Should convert AWS configuration properties")
        void shouldConvertAwsConfigurationProperties() throws Exception {
            String properties = """
                    cloud.aws.credentials.access-key=${AWS_ACCESS_KEY}
                    cloud.aws.credentials.secret-key=${AWS_SECRET_KEY}
                    cloud.aws.region.static=us-east-1
                    cloud.aws.stack.auto=false

                    # S3 Configuration
                    cloud.aws.s3.bucket=my-application-bucket
                    cloud.aws.s3.endpoint=https://s3.us-east-1.amazonaws.com

                    # SQS Configuration
                    cloud.aws.sqs.endpoint=https://sqs.us-east-1.amazonaws.com
                    cloud.aws.sqs.queues.order-queue=https://sqs.us-east-1.amazonaws.com/123456789/order-queue
                    cloud.aws.sqs.queues.notification-queue=https://sqs.us-east-1.amazonaws.com/123456789/notification-queue

                    # DynamoDB
                    cloud.aws.dynamodb.endpoint=https://dynamodb.us-east-1.amazonaws.com
                    cloud.aws.dynamodb.table-prefix=prod_

                    # Secrets Manager
                    aws.secretsmanager.enabled=true
                    aws.secretsmanager.cache.ttl=3600
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("cloud:")
                    .contains("aws:")
                    .contains("credentials:")
                    .contains("s3:")
                    .contains("sqs:")
                    .contains("dynamodb:")
                    .contains("secretsmanager:");
        }

        @Test
        @DisplayName("Should convert Kubernetes ConfigMap style properties")
        void shouldConvertKubernetesConfigMapStyleProperties() throws Exception {
            String properties = """
                    # Kubernetes-specific configurations
                    spring.cloud.kubernetes.enabled=true
                    spring.cloud.kubernetes.config.enabled=true
                    spring.cloud.kubernetes.config.name=my-app-config
                    spring.cloud.kubernetes.config.namespace=production
                    spring.cloud.kubernetes.secrets.enabled=true
                    spring.cloud.kubernetes.secrets.name=my-app-secrets
                    spring.cloud.kubernetes.reload.enabled=true
                    spring.cloud.kubernetes.reload.mode=polling
                    spring.cloud.kubernetes.reload.period=15000
                    spring.cloud.kubernetes.discovery.enabled=true
                    spring.cloud.kubernetes.discovery.all-namespaces=false
                    spring.cloud.kubernetes.loadbalancer.mode=service

                    # Liveness and Readiness
                    management.endpoint.health.probes.enabled=true
                    management.health.livenessState.enabled=true
                    management.health.readinessState.enabled=true
                    """;

            ConversionRequest request = new ConversionRequest(properties);

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            String yaml = response.getYamlContent();
            assertThat(yaml)
                    .contains("spring:")
                    .contains("cloud:")
                    .contains("kubernetes:")
                    .contains("config:")
                    .contains("secrets:")
                    .contains("reload:")
                    .contains("discovery:")
                    .contains("management:");
        }
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null properties content gracefully")
        void shouldHandleNullPropertiesContentGracefully() throws Exception {
            ConversionRequest request = new ConversionRequest(null);

            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws Exception {
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return error for invalid JSON")
        void shouldReturnErrorForInvalidJson() throws Exception {
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("not valid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle empty file upload")
        void shouldHandleEmptyFileUpload() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.properties",
                    MediaType.TEXT_PLAIN_VALUE,
                    new byte[0]
            );

            mockMvc.perform(multipart("/api/convert/file")
                            .file(emptyFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ==================== PERFORMANCE TESTS ====================

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should convert properties with 100 deeply nested levels")
        void shouldConvertPropertiesWithManyNestedLevels() throws Exception {
            StringBuilder propertyBuilder = new StringBuilder();
            StringBuilder keyBuilder = new StringBuilder();

            for (int i = 1; i <= 50; i++) {
                if (i > 1) keyBuilder.append(".");
                keyBuilder.append("level").append(i);
            }
            propertyBuilder.append(keyBuilder).append(".value=deepValue\n");

            ConversionRequest request = new ConversionRequest(propertyBuilder.toString());

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent())
                    .contains("level1:")
                    .contains("value: deepValue");
        }

        @Test
        @DisplayName("Should convert 5000 properties efficiently")
        void shouldConvert5000PropertiesEfficiently() throws Exception {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (int i = 0; i < 5000; i++) {
                propertiesBuilder.append(String.format("section%d.subsection.property%d=value%d%n",
                        i / 100, i, i));
            }

            ConversionRequest request = new ConversionRequest(propertiesBuilder.toString());

            long startTime = System.currentTimeMillis();

            MvcResult result = mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            ConversionResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), ConversionResponse.class);

            assertThat(response.getYamlContent()).isNotEmpty();
            // Should complete within 5 seconds
            assertThat(duration).isLessThan(5000);
        }
    }
}
