package com.converter.properties2yaml.controller;

import com.converter.properties2yaml.model.ConversionRequest;
import com.converter.properties2yaml.model.ConversionResponse;
import com.converter.properties2yaml.service.PropertiesToYamlConverter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/convert")
public class ConversionController {

    private final PropertiesToYamlConverter converter;

    public ConversionController(PropertiesToYamlConverter converter) {
        this.converter = converter;
    }

    /**
     * Converts properties content from request body to YAML.
     *
     * @param request the conversion request containing properties content
     * @return the conversion response with YAML content
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConversionResponse> convert(@RequestBody ConversionRequest request) {
        try {
            String yamlContent = converter.convert(request.getPropertiesContent());
            return ResponseEntity.ok(ConversionResponse.success(yamlContent));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ConversionResponse.error("Conversion failed: " + e.getMessage()));
        }
    }

    /**
     * Converts properties content from plain text to YAML.
     *
     * @param propertiesContent the properties content as plain text
     * @return the YAML content as plain text
     */
    @PostMapping(value = "/text", consumes = MediaType.TEXT_PLAIN_VALUE, produces = "text/yaml")
    public ResponseEntity<String> convertText(@RequestBody String propertiesContent) {
        try {
            String yamlContent = converter.convert(propertiesContent);
            return ResponseEntity.ok(yamlContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("# Error: " + e.getMessage());
        }
    }

    /**
     * Converts an uploaded properties file to YAML.
     *
     * @param file the uploaded properties file
     * @return the conversion response with YAML content
     */
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConversionResponse> convertFile(@RequestParam("file") MultipartFile file) {
        try {
            String propertiesContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String yamlContent = converter.convert(propertiesContent);
            return ResponseEntity.ok(ConversionResponse.success(yamlContent));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(ConversionResponse.error("Failed to read file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ConversionResponse.error("Conversion failed: " + e.getMessage()));
        }
    }

    /**
     * Downloads the converted YAML as a file.
     *
     * @param file the uploaded properties file
     * @return the YAML content as a downloadable file
     */
    @PostMapping(value = "/file/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "text/yaml")
    public ResponseEntity<String> convertFileDownload(@RequestParam("file") MultipartFile file) {
        try {
            String propertiesContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String yamlContent = converter.convert(propertiesContent);

            String originalFilename = file.getOriginalFilename();
            String outputFilename = originalFilename != null
                    ? originalFilename.replaceAll("\\.properties$", ".yaml")
                    : "converted.yaml";

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + outputFilename + "\"")
                    .body(yamlContent);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("# Error: Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("# Error: Conversion failed: " + e.getMessage());
        }
    }
}
