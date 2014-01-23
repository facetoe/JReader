package com.facetoe.jreader.githubapi.apiobjects;

import com.facetoe.jreader.githubapi.AbstractGithubQuery;

/**
 * Created by facetoe on 21/01/14.
 */
public class ObjectQuery extends AbstractGithubQuery {
    public final String objectURL;

    public ObjectQuery(String objectURL) {
        this.objectURL = objectURL;
    }

    @Override
    public String getEncodedQuery() {
        return objectURL;
    }
}
