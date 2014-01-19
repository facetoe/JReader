package com.facetoe.jreader.githubapi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by facetoe on 19/01/14.
 */
class GitHubErrorResponse {
    private String message;
    private String documentation_url;
    private GitHubError[] gitHubErrors;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDocumentation_url() {
        return documentation_url;
    }

    public void setDocumentation_url(String documentation_url) {
        this.documentation_url = documentation_url;
    }

    public GitHubError[] getGitHubErrors() {
        if(gitHubErrors == null) {
            return new GitHubError[0];
        }
        return gitHubErrors;

    }

    public void setGitHubErrors(GitHubError[] gitHubErrors) {
        this.gitHubErrors = gitHubErrors;
    }

    @Override
    public String toString() {
        return "GitHubErrorResponse{" +
                "message='" + message + '\'' +
                ", documentation_url='" + documentation_url + '\'' +
                ", gitHubErrors=" + Arrays.toString(gitHubErrors) +
                '}';
    }
}
