package com.facetoe.jreader.githubapi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by facetoe on 19/01/14.
 */

public class SearchQuery extends AbstractGithubQuery {
    private final String LANGUAGE = "java";
    private final String DEFAULT_USER = "apache";
    private final String USER;
    private final String searchQuery;

    public SearchQuery(String searchQuery) {
        USER = DEFAULT_USER;
        this.searchQuery = searchQuery;
    }

    public SearchQuery(String searchQuery, String user) {
        this.searchQuery = searchQuery;
        this.USER = user;
    }

    public String getEncodedQuery() {
        try {
            return "https://api.github.com/search/code?q=" +
                    URLEncoder.encode(searchQuery, "UTF-8") +
                    "+in:file+language:" + LANGUAGE + "+extension:java" +
                    "+user:" + USER;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}

