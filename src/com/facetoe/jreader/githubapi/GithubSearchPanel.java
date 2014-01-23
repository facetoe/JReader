
package com.facetoe.jreader.githubapi;

import com.facetoe.jreader.githubapi.apiobjects.Item;
import com.facetoe.jreader.githubapi.apiobjects.SearchResponse;
import com.facetoe.jreader.githubapi.apiobjects.TextMatch;
import com.facetoe.jreader.ui.AbstractPanel;
import com.facetoe.jreader.ui.OnTextMatchItemClickedListener;
import com.facetoe.jreader.ui.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GithubSearchPanel extends AbstractPanel {
    private JScrollPane scrollPane;
    private JPanel panel = new JPanel();
    private JLabel loadingLabel = new JLabel("Loading matches, please wait...");
    private OnTextMatchItemClickedListener listener;

    public GithubSearchPanel() {
        initComponents();
    }

    private void initComponents() {
        VerticalLayout layout = new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP);
        panel.setLayout(layout);
        panel.setBackground(Color.WHITE);
        panel.add(loadingLabel);
        scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    // TODO change this so it cancels the current task if it is called while one is already running
    public void searchGithub(final String searchTerm) {
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                syncronizedSearch(searchTerm);
                return null;
            }
        }.execute();
    }

    // This method needs to be syncronized otherwise multiple calls to searchGithub()
    // will interleave adding their TextMatchItems to the panel leading to jumbled results.
    private synchronized void syncronizedSearch(String searchTerm) {
        panel.removeAll();
        panel.add(loadingLabel);
        panel.revalidate();
        panel.repaint();
        GitHubAPI api = new GitHubAPI();
        SearchQuery query = new SearchQuery(searchTerm);
        try {
            SearchResponse response = (SearchResponse) api.sendRequest(query);
            panel.remove(loadingLabel);
            for (Item item : response.getItems()) {
                for (TextMatch textMatch : item.getText_matches()) {
                    addTextMatchItem(textMatch);
                }
            }
        } catch (GitHubAPIException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }

    private void addTextMatchItem(TextMatch match) {
        TextMatchItem item = new TextMatchItem(match);
        if(listener != null)
            item.setOnTextMatchItemClickedListener(listener);
        panel.add(item);
        panel.revalidate();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GithubSearchPanel githubSearchPanel = new GithubSearchPanel();
                githubSearchPanel.searchGithub("StringBuffer");
            }
        });
    }

    public void setOnTextMatchItemClicked(OnTextMatchItemClickedListener listener) {
        this.listener = listener;
    }
}