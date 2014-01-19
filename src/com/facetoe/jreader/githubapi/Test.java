package com.facetoe.jreader.githubapi;

import java.io.IOException;

public class Test {

    public static void main(String[] args) {
        GithubSearchQuery query = new GithubSearchQuery("StringBuffer");
        query.setSortOrder(SearchSortOrder.ASCENDING);
        System.out.println(query);
        GitHubHttpsConnection connection = new GitHubHttpsConnection();
        try {
            connection.executeQuery(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (connection.hasError()) {
            GitHubErrorResponse gitError = connection.getError();
            System.out.println(gitError.toString());
            for (GitHubError gitHubError : gitError.getGitHubErrors()) {
                System.out.println(gitHubError);
            }
        } else {
            GitHubResponse response = connection.getResponse();
            System.out.println(response);
            for (Item item : response.getItems()) {
                System.out.println(item);
            }
        }
    }
}