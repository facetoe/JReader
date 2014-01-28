
package com.facetoe.jreader.githubapi;
import com.facetoe.jreader.githubapi.apiobjects.Item;

import java.util.Collections;
import java.util.List;

public class SearchResponse extends GithubResponse {
    private List<Item> items;
    private Number total_count;

    public List<Item> getItems() {
        if (items != null) {
            return this.items;
        } else {
            return Collections.emptyList();
        }
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Number getTotal_count() {
        return this.total_count;
    }

    public void setTotal_count(Number total_count) {
        this.total_count = total_count;
    }
}
