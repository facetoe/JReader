package com.facetoe.jreader.githubapi;

import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by facetoe on 19/01/14.
 */
class GitHubHttpsConnection {
    private String USER_AGENT = "Mozilla/5.0";
    private String BASE_URL = "https://api.github.com";
    private HttpsURLConnection con;
    private GitHubResponse response;
    private GitHubErrorResponse error;
    private boolean hasError = false;
    private Gson gson = new Gson();

    public void executeQuery(GithubSearchQuery query) throws IOException {
        URL url = new URL(BASE_URL + query.toString());
        sendRequest(url);
    }

    private void sendRequest(URL url) throws IOException {
        con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        getResult();
    }

    private void getResult() throws IOException {
        if (responseOK()) {
            readResponse();
        } else {
            readError();
        }
    }

    private boolean responseOK() throws IOException {
        System.out.println(con.getResponseCode());
        return con.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    private void readResponse() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        response = gson.fromJson(read(in), GitHubResponse.class);
    }

    private void readError() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        hasError = true;
        error = gson.fromJson(read(in), GitHubErrorResponse.class);
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

    public boolean hasError() {
        return hasError;
    }

    public GitHubErrorResponse getError() {
        return error;
    }

    public GitHubResponse getResponse() {
        return response;
    }
}