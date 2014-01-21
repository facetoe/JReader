package com.facetoe.jreader.githubapi;

import com.facetoe.jreader.githubapi.apiobjects.GitHubErrorItem;
import com.facetoe.jreader.githubapi.apiobjects.GitHubErrorResponse;
import com.facetoe.jreader.githubapi.apiobjects.GithubResponse;
import com.facetoe.jreader.githubapi.apiobjects.SearchResponse;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

class GitHubAPIException extends Exception {
    public GitHubAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Created by facetoe on 19/01/14.
 */
class GitHubAPI {
    private String USER_AGENT = "Mozilla/5.0";
    private HttpsURLConnection connection;
    private GithubResponse response;
    private boolean succeeded = true;
    private Gson gson = new Gson();

    public GithubResponse sendRequest(AbstractGithubQuery query) throws GitHubAPIException {
        reset();
        try {
            URL url = generateURL(query);
            return sendRequest(url);
        } catch (Exception e) {
            throw new GitHubAPIException("Error executing query", e);
        }
    }

    private URL generateURL(AbstractGithubQuery query) throws URISyntaxException, MalformedURLException {
        return new URL(query.getEncodedQuery());
    }

    private void reset() {
        succeeded = true;
        response = null;
    }

    private GithubResponse sendRequest(URL url) throws Exception {
        System.out.print(url.toString());
        connection = (HttpsURLConnection) url.openConnection();
        initConnection();
        return getResult();
    }

    private void initConnection() throws ProtocolException {
        connection.setRequestProperty("Accept", "application/vnd.github.v3.text-match+json");
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setInstanceFollowRedirects(true);
    }

    private GithubResponse getResult() throws Exception {
        if (responseOK()) {
            return readResponse();
        } else {
            throw new GitHubAPIException(buildErrorMessage(), null);
        }
    }

    private boolean responseOK() throws IOException {
        return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    private GithubResponse readResponse() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return gson.fromJson(read(in), SearchResponse.class);
    }

    private GitHubErrorResponse readError() throws IOException {
        BufferedReader err = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        return gson.fromJson(read(err), GitHubErrorResponse.class);
    }

    private String buildErrorMessage() throws IOException {
        GitHubErrorResponse errorResponse = readError();
        String errorMessage = errorResponse.getMessage() + "\n";
        for (GitHubErrorItem errorItem : errorResponse.getGitHubErrorItems()) {
            errorMessage += "Message: " + errorItem.getMessage() + "\n";
            errorMessage += "Code: " + errorItem.getCode() + "\n";
            errorMessage += "Field: " + errorItem.getField() + "\n";
            errorMessage += "Resource:" + errorItem.getResource() + "\n";
        }
        return errorMessage;
    }

    private String read(BufferedReader in) throws IOException {
        String inputLine;
        StringBuffer response = new StringBuffer();
        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } finally {
            in.close();
        }
        return response.toString();
    }

    public boolean succeeded() {
        return succeeded;
    }

    public GithubResponse getResponse() {
        return response;
    }
}