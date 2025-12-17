package com.converter.properties2yaml.service;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class PropertiesToYamlConverter {

    /**
     * Converts a properties string to YAML format.
     *
     * @param propertiesContent the properties content as a string
     * @return the converted YAML string
     */
    public String convert(String propertiesContent) {
        Properties properties = new Properties();
        try (StringReader reader = new StringReader(propertiesContent)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse properties content", e);
        }
        return convertPropertiesToYaml(properties);
    }

    /**
     * Converts a properties file to YAML format.
     *
     * @param propertiesFile the path to the properties file
     * @return the converted YAML string
     */
    public String convertFile(Path propertiesFile) {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propertiesFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read properties file: " + propertiesFile, e);
        }
        return convertPropertiesToYaml(properties);
    }

    /**
     * Converts a properties file and saves the result to a YAML file.
     *
     * @param propertiesFile the path to the properties file
     * @param yamlFile       the path to the output YAML file
     */
    public void convertFileToFile(Path propertiesFile, Path yamlFile) {
        String yamlContent = convertFile(propertiesFile);
        try {
            Files.writeString(yamlFile, yamlContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write YAML file: " + yamlFile, e);
        }
    }

    /**
     * Converts Properties object to YAML string.
     *
     * @param properties the Properties object
     * @return the YAML string
     */
    private String convertPropertiesToYaml(Properties properties) {
        Map<String, Object> yamlMap = new TreeMap<>();

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            addToYamlMap(yamlMap, key, convertValue(value));
        }

        return toYamlString(yamlMap);
    }

    /**
     * Adds a property to the YAML map structure, handling nested keys.
     *
     * @param map   the map to add to
     * @param key   the property key (may contain dots for nesting)
     * @param value the property value
     */
    @SuppressWarnings("unchecked")
    private void addToYamlMap(Map<String, Object> map, String key, Object value) {
        String[] parts = key.split("\\.");
        Map<String, Object> currentMap = map;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];

            // Handle array notation like items[0]
            if (part.contains("[")) {
                String arrayKey = part.substring(0, part.indexOf('['));
                int index = Integer.parseInt(part.substring(part.indexOf('[') + 1, part.indexOf(']')));

                List<Object> list = (List<Object>) currentMap.computeIfAbsent(arrayKey, k -> new ArrayList<>());

                // Ensure list has enough elements
                while (list.size() <= index) {
                    list.add(new TreeMap<String, Object>());
                }

                Object element = list.get(index);
                if (element instanceof Map) {
                    currentMap = (Map<String, Object>) element;
                } else {
                    Map<String, Object> newMap = new TreeMap<>();
                    list.set(index, newMap);
                    currentMap = newMap;
                }
            } else {
                Object existing = currentMap.get(part);
                if (existing instanceof Map) {
                    currentMap = (Map<String, Object>) existing;
                } else {
                    Map<String, Object> newMap = new TreeMap<>();
                    currentMap.put(part, newMap);
                    currentMap = newMap;
                }
            }
        }

        String lastPart = parts[parts.length - 1];

        // Handle array notation in the last part
        if (lastPart.contains("[")) {
            String arrayKey = lastPart.substring(0, lastPart.indexOf('['));
            int index = Integer.parseInt(lastPart.substring(lastPart.indexOf('[') + 1, lastPart.indexOf(']')));

            List<Object> list = (List<Object>) currentMap.computeIfAbsent(arrayKey, k -> new ArrayList<>());

            while (list.size() <= index) {
                list.add(null);
            }
            list.set(index, value);
        } else {
            currentMap.put(lastPart, value);
        }
    }

    /**
     * Converts string value to appropriate type (number, boolean, or string).
     *
     * @param value the string value
     * @return the converted value
     */
    private Object convertValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        // Check for boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        // Check for integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }

        // Check for long
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }

        // Check for double
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }

        return value;
    }

    /**
     * Converts the map to a YAML string.
     *
     * @param map the map to convert
     * @return the YAML string
     */
    private String toYamlString(Map<String, Object> map) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setIndicatorIndent(2);
        options.setIndentWithIndicator(true);

        Yaml yaml = new Yaml(options);
        return yaml.dump(map);
    }
}
