package com.facetoe.jreader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 26/10/13
 * Time: 3:42 PM
 */

/**
 * Action to view the source code in a JSourcePanel.
 */
class NewSourceTabAction extends AbstractAction {
    private final JReader jReader;

    public NewSourceTabAction(JReader jReader) {
        super("View Source");
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingWorker worker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        jReader.newSourceTab();
                        return null;
                    }
                };
                worker.execute();
            }
        });

    }
}

/**
 * Action to close a tab.
 */
class CloseTabAction extends AbstractAction {
    private final JTabbedPane tabbedPane;
    private final JReader jReader;

    public CloseTabAction(JTabbedPane tabbedPane, JReader jReader) {
        this.tabbedPane = tabbedPane;
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = tabbedPane.getSelectedIndex();
        ButtonTabComponent component = ( ButtonTabComponent ) tabbedPane.getTabComponentAt(index);

        if ( component != null ) {
            component.removeTab();
            jReader.resetSearchBar();
        }
    }
}

/**
 * Action to create a new JReaderPanel tab.
 */
class NewReaderTabAction extends AbstractAction {
    private final JReader reader;

    public NewReaderTabAction(JReader reader) {
        super("New Reader Tab");
        this.reader = reader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                reader.newJReaderTab("Jreader", true);
            }
        });
    }
}

/**
 * Action to quit the application.
 */
class QuitAction extends AbstractAction {
        private final JReader jReader;
    public QuitAction(JReader jReader) {
        super("Quit");
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jReader.handleQuit();
    }
}