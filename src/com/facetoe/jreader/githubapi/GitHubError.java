package com.facetoe.jreader.githubapi;

/**
 * Created by facetoe on 19/01/14.
 */
class GitHubError {
    private String resource;
    private String code;
    private String field;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "GitHubError{" +
                "resource='" + resource + '\'' +
                ", code='" + code + '\'' +
                ", field='" + field + '\'' +
                '}';
    }
}