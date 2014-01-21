
package com.facetoe.jreader.githubapi.apiobjects;

public class GitHubErrorItem {
    private String code;
    private String field;
    private String message;
    private String resource;

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResource() {
        return this.resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "GitHubErrorItem{" +
                "code='" + code + '\'' +
                ", field='" + field + '\'' +
                ", message='" + message + '\'' +
                ", resource='" + resource + '\'' +
                '}';
    }
}
