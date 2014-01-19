
package com.facetoe.jreader.githubapi;

import java.util.List;

public class GitHubResponse{
   	private List<Item> items;
   	private Number total_count;

 	public List<Item> getItems(){
		return this.items;
	}
	public void setItems(List<Item> items){
		this.items = items;
	}
 	public Number getTotal_count(){
		return this.total_count;
	}
	public void setTotal_count(Number total_count){
		this.total_count = total_count;
	}
}
