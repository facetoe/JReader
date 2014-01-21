package com.facetoe.jreader.githubapi.apiobjects;

/**
 * Created by facetoe on 21/01/14.
 */
public class Match {
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