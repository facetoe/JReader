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
class ViewSourceAction extends AbstractAction {
    private final JReader jReader;

    public ViewSourceAction(JReader jReader) {
        super("View Source");
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                jReader.newSourceTab();
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        worker.execute();
    }
}

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

class NewReaderTabAction extends AbstractAction {
    private final JReader reader;

    public NewReaderTabAction(JReader reader) {
        super("New Reader Tab");
        this.reader = reader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        reader.newJReaderTab("Jreader", true);
    }
}

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