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
    JReader jReader;

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
                jReader.newSourceTab(null);
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        worker.execute();

    }
}

class CloseTabAction extends AbstractAction {
    JTabbedPane tabbedPane;

    public CloseTabAction(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int index = tabbedPane.getSelectedIndex();
        ButtonTabComponent component = ( ButtonTabComponent ) tabbedPane.getTabComponentAt(index);

        if ( component != null ) {
            component.removeTab();
        }
    }
}

class NewReaderTabAction extends AbstractAction {
    JReader reader;

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
        JReader jReader;
    public QuitAction(JReader jReader) {
        super("Quit");
        this.jReader = jReader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jReader.handleQuit();
    }
}