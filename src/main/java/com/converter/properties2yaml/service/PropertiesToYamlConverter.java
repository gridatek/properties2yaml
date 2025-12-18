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

    private final PropertiesParser parser = new PropertiesParser();

    /**
     * Converts a properties string to YAML format.
     *
     * @param propertiesContent the properties content as a string
     * @return the converted YAML string
     */
    public String convert(String propertiesContent) {
        return convert(propertiesContent, false);
    }

    /**
     * Converts a properties string to YAML format with optional comment preservation.
     *
     * @param propertiesContent the properties content as a string
     * @param preserveComments  whether to preserve comments from the properties file
     * @return the converted YAML string
     */
    public String convert(String propertiesContent, boolean preserveComments) {
        if (!preserveComments) {
            Properties properties = new Properties();
            try (StringReader reader = new StringReader(propertiesContent)) {
                properties.load(reader);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse properties content", e);
            }
            return convertPropertiesToYaml(properties);
        } else {
            PropertiesParser.ParseResult parseResult = parser.parse(propertiesContent);
            return convertWithComments(parseResult);
        }
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
        // Sort properties by category and then alphabetically
        List<String> sortedKeys = new ArrayList<>(properties.stringPropertyNames());
        sortedKeys.sort(new PropertyKeyComparator());

        // Use LinkedHashMap to preserve insertion order
        Map<String, Object> yamlMap = new LinkedHashMap<>();

        for (String key : sortedKeys) {
            String value = properties.getProperty(key);
            addToYamlMap(yamlMap, key, convertValue(value));
        }

        return toYamlString(yamlMap);
    }

    /**
     * Comparator for sorting property keys by category and then alphabetically.
     * Order: Spring Boot properties first, then common properties, then custom properties.
     */
    private static class PropertyKeyComparator implements Comparator<String> {
        @Override
        public int compare(String key1, String key2) {
            int priority1 = getPropertyPriority(key1);
            int priority2 = getPropertyPriority(key2);

            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }

            // Same priority, sort alphabetically
            return key1.compareTo(key2);
        }

        /**
         * Determines the priority of a property key.
         * Lower numbers = higher priority (displayed first).
         */
        private int getPropertyPriority(String key) {
            // Spring Boot core properties - highest priority
            if (key.startsWith("spring.")) {
                return 1;
            }

            // Common Spring/Boot properties
            if (key.startsWith("server.") || key.startsWith("management.") ||
                key.startsWith("logging.") || key.startsWith("security.")) {
                return 2;
            }

            // Application properties
            if (key.startsWith("app.") || key.startsWith("application.")) {
                return 3;
            }

            // Custom properties - lowest priority
            return 4;
        }
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
                String indexStr = part.substring(part.indexOf('[') + 1, part.indexOf(']'));

                // Check if the content between brackets is a valid integer
                if (isNumeric(indexStr)) {
                    int index = Integer.parseInt(indexStr);

                    List<Object> list = (List<Object>) currentMap.computeIfAbsent(arrayKey, k -> new ArrayList<>());

                    // Ensure list has enough elements
                    while (list.size() <= index) {
                        list.add(new LinkedHashMap<String, Object>());
                    }

                    Object element = list.get(index);
                    if (element instanceof Map) {
                        currentMap = (Map<String, Object>) element;
                    } else {
                        Map<String, Object> newMap = new LinkedHashMap<>();
                        list.set(index, newMap);
                        currentMap = newMap;
                    }
                } else {
                    // Treat it as a map key (e.g., [/**])
                    String mapKey = arrayKey.isEmpty() ? "[" + indexStr + "]" : arrayKey + "[" + indexStr + "]";
                    Object existing = currentMap.get(mapKey);
                    if (existing instanceof Map) {
                        currentMap = (Map<String, Object>) existing;
                    } else {
                        Map<String, Object> newMap = new LinkedHashMap<>();
                        currentMap.put(mapKey, newMap);
                        currentMap = newMap;
                    }
                }
            } else {
                Object existing = currentMap.get(part);
                if (existing instanceof Map) {
                    currentMap = (Map<String, Object>) existing;
                } else {
                    Map<String, Object> newMap = new LinkedHashMap<>();
                    currentMap.put(part, newMap);
                    currentMap = newMap;
                }
            }
        }

        String lastPart = parts[parts.length - 1];

        // Handle array notation in the last part
        if (lastPart.contains("[")) {
            String arrayKey = lastPart.substring(0, lastPart.indexOf('['));
            String indexStr = lastPart.substring(lastPart.indexOf('[') + 1, lastPart.indexOf(']'));

            // Check if the content between brackets is a valid integer
            if (isNumeric(indexStr)) {
                int index = Integer.parseInt(indexStr);

                List<Object> list = (List<Object>) currentMap.computeIfAbsent(arrayKey, k -> new ArrayList<>());

                while (list.size() <= index) {
                    list.add(null);
                }
                list.set(index, value);
            } else {
                // Treat it as a map key (e.g., [/**])
                String mapKey = arrayKey.isEmpty() ? "[" + indexStr + "]" : arrayKey + "[" + indexStr + "]";
                currentMap.put(mapKey, value);
            }
        } else {
            currentMap.put(lastPart, value);
        }
    }

    /**
     * Check if a string is numeric
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
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

    private String convertWithComments(PropertiesParser.ParseResult parseResult) {
        List<PropertyEntry> entries = parseResult.getEntries();
        List<PropertyEntry> sortedEntries = new ArrayList<>(entries);
        sortedEntries.sort(Comparator.comparing(PropertyEntry::getKey, new PropertyKeyComparator()));

        Map<String, Object> yamlMap = new LinkedHashMap<>();
        Map<String, List<String>> commentMap = new LinkedHashMap<>();

        for (PropertyEntry entry : sortedEntries) {
            String value = entry.getValue();

            List<String> comments = new ArrayList<>(entry.getPrecedingComments());

            if (value.contains(" #INLINE_COMMENT#")) {
                int index = value.indexOf(" #INLINE_COMMENT#");
                String inlineComment = value.substring(index + " #INLINE_COMMENT#".length());
                value = value.substring(0, index);
                comments.add(inlineComment);
            }

            if (!comments.isEmpty()) {
                commentMap.put(entry.getKey(), comments);
            }

            addToYamlMap(yamlMap, entry.getKey(), convertValue(value));
        }

        return renderYamlWithComments(yamlMap, commentMap, parseResult.getHeaderComments());
    }

    private String renderYamlWithComments(Map<String, Object> yamlMap,
                                          Map<String, List<String>> commentMap,
                                          List<String> headerComments) {
        StringBuilder result = new StringBuilder();

        if (!headerComments.isEmpty()) {
            for (String comment : headerComments) {
                result.append("# ").append(comment).append("\n");
            }
            result.append("\n");
        }

        renderMapWithComments(result, yamlMap, commentMap, 0, "");

        return result.toString();
    }

    @SuppressWarnings("unchecked")
    private void renderMapWithComments(StringBuilder sb, Map<String, Object> map,
                                       Map<String, List<String>> commentMap,
                                       int indentLevel, String keyPrefix) {
        String indent = "  ".repeat(indentLevel);
        boolean isFirstEntry = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullKey = keyPrefix.isEmpty() ? key : keyPrefix + "." + key;

            if (indentLevel == 0 && !isFirstEntry) {
                sb.append("\n");
            }
            isFirstEntry = false;

            List<String> comments = commentMap.get(fullKey);
            if (comments != null && !comments.isEmpty()) {
                for (String comment : comments) {
                    sb.append(indent).append("# ").append(comment).append("\n");
                }
            }

            if (value instanceof Map) {
                sb.append(indent).append(key).append(":\n");
                renderMapWithComments(sb, (Map<String, Object>) value, commentMap, indentLevel + 1, fullKey);
            } else if (value instanceof List) {
                sb.append(indent).append(key).append(":\n");
                renderListWithComments(sb, (List<Object>) value, commentMap, indentLevel + 1, fullKey);
            } else {
                sb.append(indent).append(key).append(": ");
                renderValue(sb, value);
                sb.append("\n");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void renderListWithComments(StringBuilder sb, List<Object> list,
                                        Map<String, List<String>> commentMap,
                                        int indentLevel, String keyPrefix) {
        String indent = "  ".repeat(indentLevel);

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            String fullKey = keyPrefix + "[" + i + "]";

            List<String> comments = commentMap.get(fullKey);
            if (comments != null && !comments.isEmpty()) {
                for (String comment : comments) {
                    sb.append(indent).append("# ").append(comment).append("\n");
                }
            }

            if (item instanceof Map) {
                sb.append(indent).append("-\n");
                renderMapWithComments(sb, (Map<String, Object>) item, commentMap, indentLevel + 1, fullKey);
            } else {
                sb.append(indent).append("- ");
                renderValue(sb, item);
                sb.append("\n");
            }
        }
    }

    private void renderValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            String str = (String) value;
            if (needsQuoting(str)) {
                sb.append("\"").append(escapeString(str)).append("\"");
            } else {
                sb.append(str);
            }
        } else if (value instanceof Boolean || value instanceof Number) {
            sb.append(value);
        } else {
            sb.append(value);
        }
    }

    private boolean needsQuoting(String value) {
        if (value.isEmpty()) {
            return true;
        }

        String trimmed = value.trim();
        if (!trimmed.equals(value)) {
            return true;
        }

        if (value.contains(":") || value.contains("#") || value.contains("\"") ||
            value.contains("'") || value.contains("[") || value.contains("]") ||
            value.contains("{") || value.contains("}") || value.contains(">") ||
            value.contains("|") || value.contains("&") || value.contains("*")) {
            return true;
        }

        String lower = value.toLowerCase();
        return lower.equals("true") || lower.equals("false") ||
               lower.equals("null") || lower.equals("yes") || lower.equals("no");
    }

    private String escapeString(String value) {
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
