
package com.facetoe.jreader.githubapi;

class TextMatch {
    private String object_url;
    private String object_type;
    private String property;
    private String fragment;
    private Match[] matches;

    public String getObject_url() {
        return object_url;
    }

    public void setObject_url(String object_url) {
        this.object_url = object_url;
    }

    public String getObject_type() {
        return object_type;
    }

    public void setObject_type(String object_type) {
        this.object_type = object_type;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public Match[] getMatches() {
        return matches;
    }

    public void setMatches(Match[] matches) {
        this.matches = matches;
    }
}

class Match {
    private String text;
    private int[] indices;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }
}

public class Item {
    private String git_url;
    private String html_url;
    private String name;
    private String path;
    private Repository repository;
    private Number score;
    private String sha;
    private String url;
    private TextMatch[] text_matches;

    public TextMatch[] getText_matches() {
        return text_matches;
    }

    public void setText_matches(TextMatch[] text_matches) {
        this.text_matches = text_matches;
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

    public Repository getRepository() {
        return this.repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Number getScore() {
        return this.score;
    }

    public void setScore(Number score) {
        this.score = score;
    }

    public String getSha() {
        return this.sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
