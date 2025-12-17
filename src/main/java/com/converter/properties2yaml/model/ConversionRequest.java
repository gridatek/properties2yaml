package com.converter.properties2yaml.model;

public class ConversionRequest {

    private String propertiesContent;

    public ConversionRequest() {
    }

    public ConversionRequest(String propertiesContent) {
        this.propertiesContent = propertiesContent;
    }

    public String getPropertiesContent() {
        return propertiesContent;
    }

    public void setPropertiesContent(String propertiesContent) {
        this.propertiesContent = propertiesContent;
    }
}
