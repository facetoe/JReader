package com.facetoe.jreader.githubapi;


/**
 * Sends a request for a github "object" - basically a source file.
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
