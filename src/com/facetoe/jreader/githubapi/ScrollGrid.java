
package com.facetoe.jreader.githubapi;

import com.facetoe.jreader.githubapi.apiobjects.Item;
import com.facetoe.jreader.githubapi.apiobjects.SearchResponse;
import com.facetoe.jreader.githubapi.apiobjects.TextMatch;
import com.facetoe.jreader.helpers.Utilities;
import com.facetoe.jreader.ui.VerticalLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

class TextMatchItem extends JPanel {
    JButton button;
    TextMatch match;
    RSyntaxTextArea codeArea;

    public TextMatchItem(TextMatch textMatch) {
        this.match = textMatch;
        createLayout();
    }

    private void createLayout() {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new EmptyBorder(0, 0, 5, 5), BorderFactory.createLineBorder(Color.BLACK, 1)));
        JPanel topPanel = new JPanel(new BorderLayout());

        button = createHtmlButton();
        topPanel.add(button, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);
        add(createCodeArea(), BorderLayout.CENTER);
    }

    private RSyntaxTextArea createCodeArea() {
        codeArea = new RSyntaxTextArea();
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
                System.out.println(match.getObject_url());
                System.out.print(match.getObject_url());
                System.out.print(match.getObject_type());
            }
        });
        return button;
    }
}

public class ScrollGrid {
    private ArrayList<JPanel> elements = new ArrayList<JPanel>();
    JScrollPane scrollPane = new JScrollPane();
    JPanel panel = new JPanel();
    int n = 0;


    public ScrollGrid() {
        VerticalLayout layout = new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP);
        panel.setLayout(layout);
        scrollPane.setViewportView(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        JFrame f = new JFrame("ScrollGrid");
        f.setBackground(Color.WHITE);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.add(scrollPane);


        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                GitHubAPI api = new GitHubAPI();
                GithubSearchQuery query = new GithubSearchQuery("for (int i = 0; i < len; i++)");
                try {
                    SearchResponse response = (SearchResponse)api.sendRequest(query);
                    for (Item item : response.getItems()) {
                        for (TextMatch textMatch : item.getText_matches()) {
                            addTextMatchItem(textMatch);
                        }
                    }
                } catch (GitHubAPIException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        worker.execute();


        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void addTextMatchItem(TextMatch match) {
        panel.add(new TextMatchItem(match));
        panel.revalidate();
    }


    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ScrollGrid();
            }
        });
    }
}