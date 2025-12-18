# Properties to YAML Converter

[![CI](https://github.com/gridatek/properties2yaml/actions/workflows/ci.yml/badge.svg)](https://github.com/gridatek/properties2yaml/actions/workflows/ci.yml)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)

A Spring Boot application that converts Java properties files to YAML format. This tool provides both REST API endpoints and supports various input formats including JSON, plain text, and file uploads.

## Features

- 🔄 Convert properties to YAML format
- 📝 Multiple input formats: JSON, plain text, and file upload
- 🌳 Automatically handles nested properties with dot notation
- 📊 Supports array notation (e.g., `items[0].name`)
- 🗺️ Supports map keys with special characters (e.g., `[/**]`)
- 🎯 Smart type detection (numbers, booleans, strings)
- 📥 Download converted YAML files
- 🧪 Comprehensive test coverage
- ✅ CI/CD with GitHub Actions

## Requirements

- Java 17 or higher
- Maven 3.6+
- Spring Boot 3.2.0

## Installation

### Clone the repository

```bash
git clone https://github.com/gridatek/properties2yaml.git
cd properties2yaml
```

### Build the project

```bash
mvn clean package
```

## Usage

### Running the application

```bash
mvn spring-boot:run
```

Or run the JAR file:

```bash
java -jar target/properties2yaml-1.0.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Convert JSON Request

**POST** `/api/convert`

Convert properties content sent as JSON.

**Request:**
```bash
curl -X POST http://localhost:8080/api/convert \
  -H "Content-Type: application/json" \
  -d '{
    "propertiesContent": "server.port=8080\nserver.address=localhost"
  }'
```

**Response:**
```json
{
  "success": true,
  "yamlContent": "server:\n  port: 8080\n  address: localhost\n",
  "error": null
}
```

### 2. Convert Plain Text

**POST** `/api/convert/text`

Convert properties content sent as plain text.

**Request:**
```bash
curl -X POST http://localhost:8080/api/convert/text \
  -H "Content-Type: text/plain" \
  -d "server.port=8080
server.address=localhost"
```

**Response:**
```yaml
server:
  port: 8080
  address: localhost
```

### 3. Convert File

**POST** `/api/convert/file`

Upload a properties file for conversion.

**Request:**
```bash
curl -X POST http://localhost:8080/api/convert/file \
  -F "file=@application.properties"
```

**Response:**
```json
{
  "success": true,
  "yamlContent": "server:\n  port: 8080\n  address: localhost\n",
  "error": null
}
```

### 4. Convert File with Download

**POST** `/api/convert/file/download`

Upload a properties file and download the YAML result.

**Request:**
```bash
curl -X POST http://localhost:8080/api/convert/file/download \
  -F "file=@application.properties" \
  -O -J
```

This will download a file named `application.yaml` with the converted content.

## Conversion Examples

### Simple Properties

**Input:**
```properties
server.port=8080
server.address=localhost
app.name=MyApplication
```

**Output:**
```yaml
server:
  port: 8080
  address: localhost
app:
  name: MyApplication
```

### Nested Properties

**Input:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update
```

**Output:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: secret
  jpa:
    hibernate:
      ddl-auto: update
```

### Array Properties

**Input:**
```properties
items[0].name=Item 1
items[0].price=10.99
items[1].name=Item 2
items[1].price=20.50
```

**Output:**
```yaml
items:
- name: Item 1
  price: 10.99
- name: Item 2
  price: 20.5
```

### Complex Spring Cloud Gateway Configuration

**Input:**
```properties
spring.cloud.gateway.routes[0].id=user-service
spring.cloud.gateway.routes[0].uri=lb://user-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origins=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-methods=GET,POST
```

**Output:**
```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: user-service
        uri: lb://user-service
        predicates:
        - Path=/api/users/**
      globalcors:
        cors-configurations:
          "[/**]":
            allowed-origins: '*'
            allowed-methods: GET,POST
```

## Type Detection

The converter automatically detects and converts data types:

- **Numbers**: `port=8080` → `port: 8080`
- **Booleans**: `enabled=true` → `enabled: true`
- **Strings**: `name=MyApp` → `name: MyApp`

## Building from Source

### Compile only

```bash
mvn clean compile
```

### Run tests

```bash
mvn test
```

### Run integration tests

```bash
mvn verify
```

### Generate coverage report

```bash
mvn test jacoco:report
```

The coverage report will be available at `target/site/jacoco/index.html`

### Package application

```bash
mvn package
```

This creates an executable JAR file in the `target/` directory.

## Testing

The project includes comprehensive tests:

- **Unit Tests**: Test individual components and conversion logic
- **Integration Tests**: Test REST API endpoints and file handling
- **Complex Test Cases**: Test real-world configurations (Spring Boot, microservices, etc.)

Run all tests:

```bash
mvn clean verify
```

## CI/CD

The project uses GitHub Actions for continuous integration:

- **Build and Test**: Runs on Java 17 and 21
- **Code Quality**: Generates test coverage reports
- **Integration Tests**: Validates API endpoints
- **Dependency Check**: Monitors for dependency updates

View the [CI workflow](.github/workflows/ci.yml) for details.

## Project Structure

```
properties2yaml/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/converter/properties2yaml/
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── model/           # Request/Response models
│   │   │       ├── service/         # Conversion service
│   │   │       └── Properties2YamlApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/converter/properties2yaml/
│               ├── integration/     # Integration tests
│               └── service/         # Unit tests
├── .github/
│   └── workflows/
│       └── ci.yml                   # CI/CD configuration
├── pom.xml                          # Maven configuration
└── README.md
```

## Technology Stack

- **Spring Boot 3.2.0**: Application framework
- **SnakeYAML 2.2**: YAML processing
- **Lombok**: Reduce boilerplate code
- **JUnit 5**: Testing framework
- **Maven**: Build tool
- **JaCoCo**: Code coverage

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- YAML processing by [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml)

## Support

For issues, questions, or contributions, please visit the [GitHub repository](https://github.com/gridatek/properties2yaml).
