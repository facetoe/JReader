/*   
*    Copyright (C) 2013  facetoe - facetoe@ymail.com
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License along
*    with this program; if not, write to the Free Software Foundation, Inc.,
*    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package com.facetoe.jreader.helpers;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.File;
import java.util.ArrayList;

/**
 * Unzips a file and provides feedback on progress.
 */
public class UnZipper {
    private final ArrayList<ZipProgressListener> listeners = new ArrayList<ZipProgressListener>();
    private boolean isCanceled = false;

    public void unzip(File sourceFile, String destinationPath) throws Exception {
        isCanceled = false;
        ZipFile zipFile = new ZipFile(sourceFile);
        zipFile.setRunInThread(true);
        zipFile.extractAll(destinationPath);
        ProgressMonitor pm = zipFile.getProgressMonitor();
        while (pm.getState() == ProgressMonitor.STATE_BUSY) {
            if (isCanceled)
                return;

            if (pm.getCurrentOperation() == ProgressMonitor.OPERATION_EXTRACT) {
                System.out.println(pm.getFileName());
                publishProgress(pm.getFileName());
            }
        }

        if (pm.getResult() == ProgressMonitor.RESULT_ERROR) {
            throw new Exception(pm.getException());
        }
    }

    public void cancel() {
        isCanceled = true;
    }

    private void publishProgress(String message) {
        for (ZipProgressListener listener : listeners) {
            listener.updateProgress(message);
        }
    }

    public void addZipProgressListener(ZipProgressListener listener) {
        listeners.add(listener);
    }
}
