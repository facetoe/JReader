package com.facetoe.jreader.githubapi;

import com.facetoe.jreader.githubapi.apiobjects.*;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by facetoe on 19/01/14.
 */
public class GitHubAPI {
    private static final String USER_AGENT = "Mozilla/5.0";
    private static HttpsURLConnection connection;
    private static final Gson gson = new Gson();
    private static AbstractGithubQuery query;

    public static GithubResponse sendRequest(AbstractGithubQuery query) throws GitHubAPIException {
        try {
            URL url = generateURL(query);
            return sendRequest(url);
        } catch (Exception e) {
            throw new GitHubAPIException("Error executing query: " + e.getMessage(), e);
        }
    }

    private static URL generateURL(AbstractGithubQuery githubQuery) throws MalformedURLException {
        query = githubQuery;
        return new URL(query.getEncodedQuery());
    }

    private static GithubResponse sendRequest(URL url) throws Exception {
        System.out.println(url.toString());
        connection = (HttpsURLConnection) url.openConnection();
        initConnection();
        return getResponse();
    }

    private static void initConnection() throws ProtocolException {
        connection.setRequestProperty("Accept", "application/vnd.github.v3.text-match+json");
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setInstanceFollowRedirects(true);
    }

    private static GithubResponse getResponse() throws Exception {
        if (responseOK()) {
            return readResponse();
        } else {
            throw new GitHubAPIException(buildErrorMessage(), null);
        }
    }

    private static boolean responseOK() throws IOException {
        return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    private static GithubResponse readResponse() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        if (query instanceof SearchQuery) {
            return gson.fromJson(read(in), SearchResponse.class);
        } else if (query instanceof ObjectQuery) {
            return gson.fromJson(read(in), ObjectResponse.class);
        } else {
            throw new UnsupportedOperationException("What have you done!");
        }
    }

    private static String buildErrorMessage() throws IOException {
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

    private static GitHubErrorResponse readError() throws IOException {
        BufferedReader err = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        return gson.fromJson(read(err), GitHubErrorResponse.class);
    }

    private static String read(BufferedReader in) throws IOException {
        String inputLine;
        StringBuilder response = new StringBuilder();
        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } finally {
            in.close();
        }
        return response.toString();
    }
}