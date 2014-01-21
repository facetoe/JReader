package com.facetoe.jreader.githubapi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by facetoe on 19/01/14.
 */

class GithubSearchQuery extends AbstractGithubQuery {
    private final String BASE_URL = "https://api.github.com";
    private final String SEARCH_PATH = "/search/code?";
    private final String LANGUAGE = "java";
    private final String DEFAULT_USER = "apache";
    private final String USER;

    private String searchQuery;

    public GithubSearchQuery(String searchQuery) {
        USER = DEFAULT_USER;
        this.searchQuery = searchQuery;
    }

    public GithubSearchQuery(String searchQuery, String user) {
        this.searchQuery = searchQuery;
        this.USER = user;
    }

    public String getEncodedQuery() {
        try {
            return BASE_URL +
                    SEARCH_PATH +
                    "q=" + URLEncoder.encode(searchQuery, "UTF-8")
                    + "+in:file+language:" +
                    LANGUAGE + "+extension:java" + "+user:" + USER;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}

