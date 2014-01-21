
package com.facetoe.jreader.githubapi;

import com.facetoe.jreader.githubapi.apiobjects.*;
import com.facetoe.jreader.helpers.Utilities;
import com.facetoe.jreader.ui.VerticalLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

interface OnTextMatchItemClickedListener {
    void textMatchItemClicked(String objectUrl);
}

class TextMatchItem extends JPanel {
    private TextMatch match;
    private OnTextMatchItemClickedListener listener;

    public TextMatchItem(TextMatch textMatch) {
        this.match = textMatch;
        createLayout();
    }

    private void createLayout() {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new EmptyBorder(0, 0, 5, 5), BorderFactory.createLineBorder(Color.BLACK, 1)));
        JPanel topPanel = new JPanel(new BorderLayout());

        JButton button = createHtmlButton();
        topPanel.add(button, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);
        add(createCodeArea(), BorderLayout.CENTER);
    }

    private RSyntaxTextArea createCodeArea() {
        RSyntaxTextArea codeArea = new RSyntaxTextArea();
        Theme theme = Utilities.loadTheme();
        theme.apply(codeArea);

        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(false);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setEditable(false);
        codeArea.setText(match.getFragment());
        return codeArea;
    }

    private JButton createHtmlButton() {
        String url = match.getObject_url();
        String fileName = Utilities.extractFileName(url);
        String html = "<HTML><a href=\"\">" + fileName + "</a></HTML>";
        JButton button = new JButton(html);

        button.setToolTipText("Click to view " + fileName);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.textMatchItemClicked(match.getObject_url());
                }
            }
        });
        return button;
    }

    public void setOnTextMatchItemClickedListener(OnTextMatchItemClickedListener listener) {
        this.listener = listener;
    }
}

public class GithubSearchPanel implements OnTextMatchItemClickedListener {
    private JScrollPane scrollPane = new JScrollPane();
    private JPanel panel = new JPanel();
    private JLabel loadingLabel = new JLabel("Loading, please wait...");

    public GithubSearchPanel() {
        initComponents();
        initFrame();
    }

    private void initComponents() {
        VerticalLayout layout = new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP);
        panel.setLayout(layout);
        panel.setBackground(Color.WHITE);
        scrollPane.setViewportView(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
    }

    private void initFrame() {
        JFrame frame = new JFrame("GithubSearchPanel");
        frame.setBackground(Color.WHITE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(scrollPane);
        frame.pack();
        frame.setVisible(true);
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
            SearchResponse response = (SearchResponse)api.sendRequest(query);
            panel.remove(loadingLabel);
            for (Item item : response.getItems()) {
                for (TextMatch textMatch : item.getText_matches()) {
                    addTextMatchItem(textMatch);
                }
            }
        } catch (GitHubAPIException e) {
            e.printStackTrace();
        }
    }

    private void addTextMatchItem(TextMatch match) {
        TextMatchItem item = new TextMatchItem(match);
        item.setOnTextMatchItemClickedListener(this);
        panel.add(item);
        panel.revalidate();
    }

    @Override
    public void textMatchItemClicked(String objectUrl) {
        GitHubAPI api = new GitHubAPI();
        ObjectQuery query = new ObjectQuery(objectUrl);
        try {
            ObjectResponse response = (ObjectResponse)api.sendRequest(query);
            URL url = new URL(response.getHtml_url()
                    .replace("https://github.com", "https://raw.github.com")
            .replace("/blob", ""));

            try {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while((line = in.readLine()) != null)
                    System.out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (GitHubAPIException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
}