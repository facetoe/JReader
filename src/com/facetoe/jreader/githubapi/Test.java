package com.facetoe.jreader.githubapi;

import com.facetoe.jreader.githubapi.apiobjects.Item;
import com.facetoe.jreader.githubapi.apiobjects.Match;
import com.facetoe.jreader.githubapi.apiobjects.TextMatch;

import java.util.Arrays;

public class Test {

//    public static void main(String[] args) {
//        SearchQuery query = new SearchQuery("StringBuffer sb = new StringBuffer();");
//        GitHubAPI connection = new GitHubAPI();
//        try {
//            SearchResponse response = (connection.sendRequest(query);
//            for (Item item : response.getItems()) {
//                printItem(item);
//            }
//
//            query = new SearchQuery("class Activity");
//            response = connection.sendRequest(query);
//            for (Item item : response.getItems()) {
//                printItem(item);
//            }
//
//
//        } catch (GitHubAPIException e) {
//            System.out.println(e.getMessage());
//            System.out.print(e.getCause().getMessage());
//        }
//    }

    private static void printItem(Item item) {
        System.out.println("Item name: " + item.getName());
        for (TextMatch textMatch : item.getText_matches()) {
            System.out.println("Object type: " + textMatch.getObject_type());
            System.out.println("Object url: " + textMatch.getObject_url());
            System.out.println("Object property: " + textMatch.getProperty());
            System.out.println("Fragment: \n" + textMatch.getFragment());
            Match[] matches = textMatch.getMatches();
            for (Match match : matches) {
                System.out.println(Arrays.toString(match.getIndices()));
            }
            System.out.println();
        }
    }
}