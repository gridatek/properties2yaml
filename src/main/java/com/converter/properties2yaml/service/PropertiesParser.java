package com.converter.properties2yaml.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

class PropertiesParser {

    public ParseResult parse(String propertiesContent) {
        List<PropertyEntry> entries = new ArrayList<>();
        List<String> headerComments = new ArrayList<>();
        List<String> accumulatedComments = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(propertiesContent))) {
            String line;
            int lineNumber = 0;
            boolean foundFirstProperty = false;
            StringBuilder multilineValue = null;
            String multilineKey = null;
            List<String> multilineComments = null;
            int multilineStartLine = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();

                if (multilineValue != null) {
                    if (line.endsWith("\\")) {
                        multilineValue.append("\n").append(line, 0, line.length() - 1);
                    } else {
                        multilineValue.append("\n").append(line);
                        entries.add(new PropertyEntry(multilineKey, multilineValue.toString(),
                                                     multilineComments, multilineStartLine));
                        multilineValue = null;
                        multilineKey = null;
                        multilineComments = null;
                    }
                    continue;
                }

                if (trimmed.isEmpty()) {
                    if (!foundFirstProperty && !accumulatedComments.isEmpty()) {
                        headerComments.addAll(accumulatedComments);
                        accumulatedComments.clear();
                    }
                    continue;
                }

                if (isComment(trimmed)) {
                    String comment = extractComment(trimmed);
                    accumulatedComments.add(comment);
                    continue;
                }

                PropertyLine propertyLine = parseLine(line);
                if (propertyLine != null) {
                    foundFirstProperty = true;

                    if (propertyLine.value.endsWith("\\")) {
                        multilineKey = propertyLine.key;
                        multilineValue = new StringBuilder(propertyLine.value.substring(0, propertyLine.value.length() - 1));
                        multilineComments = new ArrayList<>(accumulatedComments);
                        multilineStartLine = lineNumber;
                    } else {
                        entries.add(new PropertyEntry(propertyLine.key, propertyLine.value,
                                                     accumulatedComments, lineNumber));
                    }
                    accumulatedComments.clear();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse properties content", e);
        }

        return new ParseResult(entries, headerComments);
    }

    private boolean isComment(String line) {
        return line.startsWith("#") || line.startsWith("!");
    }

    private String extractComment(String line) {
        if (line.startsWith("#") || line.startsWith("!")) {
            return line.substring(1).trim();
        }
        return line;
    }

    private PropertyLine parseLine(String line) {
        int commentIndex = findCommentStart(line);
        String effectiveLine = commentIndex >= 0 ? line.substring(0, commentIndex).trim() : line.trim();
        String inlineComment = commentIndex >= 0 ? line.substring(commentIndex + 1).trim() : null;

        if (effectiveLine.isEmpty()) {
            return null;
        }

        int separatorIndex = findSeparator(effectiveLine);
        if (separatorIndex < 0) {
            return null;
        }

        String key = effectiveLine.substring(0, separatorIndex).trim();
        String value = effectiveLine.substring(separatorIndex + 1).trim();

        value = unescapeValue(value);

        if (inlineComment != null && !inlineComment.isEmpty()) {
            value = value + " #INLINE_COMMENT#" + inlineComment;
        }

        return new PropertyLine(key, value);
    }

    private int findCommentStart(String line) {
        boolean inEscape = false;
        boolean inQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (inEscape) {
                inEscape = false;
                continue;
            }

            if (ch == '\\') {
                inEscape = true;
                continue;
            }

            if (ch == '"') {
                inQuote = !inQuote;
                continue;
            }

            if (!inQuote && (ch == '#' || ch == '!')) {
                return i;
            }
        }

        return -1;
    }

    private int findSeparator(String line) {
        boolean inEscape = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (inEscape) {
                inEscape = false;
                continue;
            }

            if (ch == '\\') {
                inEscape = true;
                continue;
            }

            if (ch == '=' || ch == ':') {
                return i;
            }

            if (ch == ' ' || ch == '\t') {
                return i;
            }
        }

        return -1;
    }

    private String unescapeValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        StringBuilder result = new StringBuilder();
        boolean inEscape = false;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (inEscape) {
                switch (ch) {
                    case 'n' -> result.append('\n');
                    case 't' -> result.append('\t');
                    case 'r' -> result.append('\r');
                    case 'f' -> result.append('\f');
                    case '\\' -> result.append('\\');
                    default -> result.append(ch);
                }
                inEscape = false;
            } else if (ch == '\\') {
                inEscape = true;
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    static class ParseResult {
        private final List<PropertyEntry> entries;
        private final List<String> headerComments;

        public ParseResult(List<PropertyEntry> entries, List<String> headerComments) {
            this.entries = entries;
            this.headerComments = headerComments;
        }

        public List<PropertyEntry> getEntries() {
            return entries;
        }

        public List<String> getHeaderComments() {
            return headerComments;
        }
    }

    private static class PropertyLine {
        final String key;
        final String value;

        PropertyLine(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
