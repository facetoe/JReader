
package com.facetoe.jreader.ui;

import com.facetoe.jreader.githubapi.GitHubAPI;
import com.facetoe.jreader.githubapi.GithubAPIException;
import com.facetoe.jreader.githubapi.SearchQuery;
import com.facetoe.jreader.githubapi.SearchResponse;
import com.facetoe.jreader.githubapi.apiobjects.Item;
import com.facetoe.jreader.githubapi.apiobjects.TextMatch;
import com.facetoe.jreader.helpers.Util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * This class defines a panel for displaying Github search results.
 */
public class GithubSearchPanel extends JPanel {
    private final JPanel panel = new JPanel();
    private final JLabel loadingLabel = new JLabel("Loading matches, please wait...");
    private final JLabel nothingFoundLabel = new JLabel("No results found.");
    private OnTextMatchItemClickedListener itemClickedListener;
    private final ArrayList<StatusUpdateListener> statusUpdateListeners = new ArrayList<StatusUpdateListener>();

    public GithubSearchPanel() {
        initComponents();
    }

    private void initComponents() {
        VerticalLayout layout = new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP);
        panel.setLayout(layout);
        panel.add(loadingLabel);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Increase the scroll speed as otherwise it is too slow.
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
    // will interleave adding their TextMatchItems to the panel resuting in jumbled results.
    private synchronized void syncronizedSearch(String searchTerm) {
        updateStatus("Searching for " + searchTerm);
        updateProgress(0);
        resetPanel();
        panel.add(loadingLabel);
        try {
            SearchResponse response = (SearchResponse) GitHubAPI.sendRequest(new SearchQuery(searchTerm));
            panel.remove(loadingLabel);
            updateStatus("");
            populatePanel(response);
            if (panel.getComponents().length == 0) {
                panel.add(nothingFoundLabel);
                panel.revalidate();
                panel.repaint();
            }
        } catch (GithubAPIException e) {
            e.printStackTrace();
        }
    }

    private void resetPanel() {
        panel.removeAll();
        panel.revalidate();
        panel.repaint();
    }

    private void populatePanel(SearchResponse response) {
        int totalMatches = countTextMatchItems(response);
        updateStatus("Found " + totalMatches + " matches");
        int cnt = 0;
        for (Item item : response.getItems()) {
            for (TextMatch textMatch : item.getText_matches()) {
                updateProgress((int) Util.percent(++cnt, totalMatches));
                addTextMatchItem(textMatch);
            }
        }
    }

    private int countTextMatchItems(SearchResponse response) {
        int n = 0;
        for (Item item : response.getItems()) {
            n += item.getText_matches().length;
        }
        return n;
    }

    private void addTextMatchItem(TextMatch match) {
        TextMatchItem item = new TextMatchItem(match);
        if (itemClickedListener != null)
            item.setOnTextMatchItemClickedListener(itemClickedListener);
        panel.add(item);
        panel.revalidate();
    }

    public void setOnTextMatchItemClickedListener(OnTextMatchItemClickedListener listener) {
        this.itemClickedListener = listener;
    }

    public void addStatusUpdateListener(StatusUpdateListener listener) {
        statusUpdateListeners.add(listener);
    }

    void updateStatus(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (StatusUpdateListener statusUpdateListener : statusUpdateListeners) {
                    statusUpdateListener.updateStatus(message);
                }
            }
        });
    }

    void updateProgress(final int progress) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (StatusUpdateListener statusUpdateListener : statusUpdateListeners) {
                    statusUpdateListener.updateProgressBar(progress);
                }
            }
        });
    }
}