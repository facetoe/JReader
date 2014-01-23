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

        if(indices != null) {
            return indices;
        } else {
            return new int[0];
        }
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }
}