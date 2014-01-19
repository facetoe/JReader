package com.facetoe.jreader.githubapi;

/**
 * Created by facetoe on 19/01/14.
 */

enum SearchSortType {
    LAST_INDEXED,
    NONE
}

enum SearchSortOrder {
    ASCENDING,
    DESCENDING,
    NONE
}

class GithubSearchQuery {
    private final String searchURL = "/search/code?";
    private String language = "java";

    private String searchQuery;
    private SearchSortType sortType = SearchSortType.NONE;
    private SearchSortOrder sortOrder = SearchSortOrder.NONE;

    public GithubSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setSortType(SearchSortType sortType) {
        this.sortType = sortType;
    }

    public void setSortOrder(SearchSortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return searchURL + "q=" + searchQuery + "+in:file+language:" + language + "+repo:facetoe/jreader";

    }
}

