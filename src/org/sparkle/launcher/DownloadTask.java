package org.sparkle.launcher;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
 
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
 
/**
 * Execute file download in a background thread and update the progress.
 * @author www.codejava.net
 *
 */
public class DownloadTask extends SwingWorker<Void, Void> {
    private static final int BUFFER_SIZE = 4096;   
    private String downloadURL;
    private String saveDirectory;
    private LauncherForm gui;
     
    public DownloadTask(LauncherForm gui, String downloadURL, String saveDirectory) {
        this.gui = gui;
        this.downloadURL = downloadURL;
        this.saveDirectory = saveDirectory;
    }
     
    /**
     * Executed in background thread
     */
    @Override
    protected Void doInBackground() throws Exception {
        try {
            HTTPDownloadUtil util = new HTTPDownloadUtil();
            util.downloadFile(downloadURL);
             
            // set file information on the GUI
             
            String saveFilePath = saveDirectory + File.separator + util.getFileName();
 
            InputStream inputStream = util.getInputStream();
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            long totalBytesRead = 0;
            int percentCompleted = 0;
            long fileSize = util.getContentLength();
 
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                percentCompleted = (int) (totalBytesRead * 100 / fileSize);
 
                gui.loaded = (float)percentCompleted/100;
            }
 
            outputStream.close();
 
            util.disconnect();
        } catch (IOException ex) {
                  
            ex.printStackTrace();
            cancel(true);          
        }
        return null;
    }
 
    /**
     * Executed in Swing's event dispatching thread
     */
    @Override
    protected void done() {
        if (!isCancelled()) {
            JOptionPane.showMessageDialog(gui,
                    "File has been downloaded successfully!", "Message",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }  
}
