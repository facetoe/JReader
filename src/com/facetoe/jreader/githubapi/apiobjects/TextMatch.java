package com.facetoe.jreader.githubapi.apiobjects;

/**
 * Created by facetoe on 21/01/14.
 */
public class TextMatch {
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

        if (matches != null) {
            return matches;
        } else {
            return new Match[0];
        }
    }

    public void setMatches(Match[] matches) {
        this.matches = matches;
    }
}