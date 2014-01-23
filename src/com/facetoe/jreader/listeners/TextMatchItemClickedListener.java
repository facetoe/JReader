package com.facetoe.jreader.listeners;

import com.facetoe.jreader.githubapi.GitHubAPI;
import com.facetoe.jreader.githubapi.GitHubAPIException;
import com.facetoe.jreader.githubapi.apiobjects.ObjectQuery;
import com.facetoe.jreader.githubapi.apiobjects.ObjectResponse;
import com.facetoe.jreader.helpers.Utilities;
import com.facetoe.jreader.ui.JReader;
import com.facetoe.jreader.ui.OnTextMatchItemClickedListener;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * JReader
 * Created by facetoe on 23/01/14.
 */
public class TextMatchItemClickedListener implements OnTextMatchItemClickedListener {
    JReader jreader;
    String searchTerm;
    public TextMatchItemClickedListener(JReader jreader, String searchTerm) {
        this.jreader = jreader;
        this.searchTerm = searchTerm;
    }

    @Override
    public void textMatchItemClicked(String objectUrl) {
        try {
            ObjectResponse response = (ObjectResponse) GitHubAPI.sendRequest(new ObjectQuery(objectUrl))                            ;
            URL url = new URL(response.getHtml_url()
                    .replace("https://github.com", "https://raw.github.com")
                    .replace("/blob", ""));
            String title = Utilities.extractFileName(url.getFile());
            jreader.createAndShowNewSourcePanel(url, title);
        } catch (GitHubAPIException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}