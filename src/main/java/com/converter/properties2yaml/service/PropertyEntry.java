package com.converter.properties2yaml.service;

import java.util.ArrayList;
import java.util.List;

class PropertyEntry {
    private final String key;
    private final String value;
    private final List<String> precedingComments;
    private final int lineNumber;

    public PropertyEntry(String key, String value, List<String> precedingComments, int lineNumber) {
        this.key = key;
        this.value = value;
        this.precedingComments = new ArrayList<>(precedingComments);
        this.lineNumber = lineNumber;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public List<String> getPrecedingComments() {
        return new ArrayList<>(precedingComments);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean hasComments() {
        return !precedingComments.isEmpty();
    }
}
