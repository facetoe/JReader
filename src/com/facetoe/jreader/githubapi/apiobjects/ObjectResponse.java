
package com.facetoe.jreader.githubapi.apiobjects;


public class ObjectResponse {
    private Links links;
    private String content;
    private String encoding;
    private String git_url;
    private String html_url;
    private String name;
    private String path;
    private String sha;
    private Number size;
    private String type;
    private String url;

    public Links getLinks() {
        return this.links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getGit_url() {
        return this.git_url;
    }

    public void setGit_url(String git_url) {
        this.git_url = git_url;
    }

    public String getHtml_url() {
        return this.html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSha() {
        return this.sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Number getSize() {
        return this.size;
    }

    public void setSize(Number size) {
        this.size = size;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
