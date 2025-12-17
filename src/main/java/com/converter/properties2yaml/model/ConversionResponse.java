package com.converter.properties2yaml.model;

public class ConversionResponse {

    private String yamlContent;
    private boolean success;
    private String errorMessage;

    public ConversionResponse() {
    }

    public static ConversionResponse success(String yamlContent) {
        ConversionResponse response = new ConversionResponse();
        response.setYamlContent(yamlContent);
        response.setSuccess(true);
        return response;
    }

    public static ConversionResponse error(String errorMessage) {
        ConversionResponse response = new ConversionResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public String getYamlContent() {
        return yamlContent;
    }

    public void setYamlContent(String yamlContent) {
        this.yamlContent = yamlContent;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
