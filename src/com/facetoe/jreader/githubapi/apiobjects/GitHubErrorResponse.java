
package com.facetoe.jreader.githubapi.apiobjects;

import java.util.Collections;
import java.util.List;

public class GitHubErrorResponse extends GithubResponse {
    private String documentation_url;
    private List<GitHubErrorItem> gitHubErrorItems;
    private String message;

    public String getDocumentation_url() {
        return this.documentation_url;
    }

    public void setDocumentation_url(String documentation_url) {
        this.documentation_url = documentation_url;
    }

    public List<GitHubErrorItem> getGitHubErrorItems() {
        if (gitHubErrorItems != null)
            return this.gitHubErrorItems;
        else
            return Collections.emptyList();
    }

    public void setGitHubErrorItems(List<GitHubErrorItem> gitHubErrorItems) {
        this.gitHubErrorItems = gitHubErrorItems;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "GitHubErrorResponse{" +
                "documentation_url='" + documentation_url + '\'' +
                ", gitHubErrorItems=" + gitHubErrorItems +
                ", message='" + message + '\'' +
                '}';
    }
}
