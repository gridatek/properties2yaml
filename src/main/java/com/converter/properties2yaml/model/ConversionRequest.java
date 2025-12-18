package com.converter.properties2yaml.model;

public class ConversionRequest {

    private String propertiesContent;
    private boolean preserveComments = false;

    public ConversionRequest() {
    }

    public ConversionRequest(String propertiesContent) {
        this.propertiesContent = propertiesContent;
    }

    public ConversionRequest(String propertiesContent, boolean preserveComments) {
        this.propertiesContent = propertiesContent;
        this.preserveComments = preserveComments;
    }

    public String getPropertiesContent() {
        return propertiesContent;
    }

    public void setPropertiesContent(String propertiesContent) {
        this.propertiesContent = propertiesContent;
    }

    public boolean isPreserveComments() {
        return preserveComments;
    }

    public void setPreserveComments(boolean preserveComments) {
        this.preserveComments = preserveComments;
    }
}
