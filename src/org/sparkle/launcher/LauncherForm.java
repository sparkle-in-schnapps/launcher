package org.sparkle.launcher;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 *
 * @author yew_mentzaki
 */
public class LauncherForm extends JFrame {

    String projectUrl, targetPath, title;

    public LauncherForm(String title, String projectUrl, String targetPath) {
        this.projectUrl = projectUrl;
        File target = new File(System.getProperty("user.home") + "/" + targetPath);
        target.mkdirs();
        this.targetPath = target.getAbsolutePath() + System.getProperty("file.separator");
        this.title = title;
        setUndecorated(true);
        setTitle(title);
        setSize(300, 200);
        setBackground(Color.black);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try {
            setIconImage(ImageIO.read(LauncherForm.class.getResourceAsStream("icon.png")));
            background = ImageIO.read(LauncherForm.class.getResourceAsStream("background.png"));
        } catch (Exception e) {
        }
        repaint();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        this.setLocation(x, y);
        setVisible(true);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clicked(evt);
            }
        });
        Thread thread = new Thread("Updating thread") {

            @Override
            public void run() {
                try {
                    thread();
                } catch (Exception e) {
                    e.printStackTrace();
                    this.stop();
                }
            }

        };
        Timer timer = new Timer(20, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        thread.start();
        timer.start();
    }

    public void thread() throws IOException {
        if (!new File(targetPath + "lib").exists()) {

            try {
                if (new File(targetPath + "lib.zip").exists()) {
                    new File(targetPath + "lib.zip").delete();
                }
                DownloadTask dt = new DownloadTask(this, projectUrl + "lib.zip", targetPath);
                status = "Downloading libraries...";
                dt.doInBackground();
                status = "Unpacking libraries...";
                UnZip.unzip(targetPath + "lib.zip", targetPath);
                new File(targetPath + "lib.zip").delete();

            } catch (Exception ex) {
                Logger.getLogger(LauncherForm.class.getName()).log(Level.SEVERE, null, ex);
                status = "Error: " + ex;
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }
                System.exit(0);
            }
        }
        status = "Checking for updates...";
        boolean update = true;
        try {
            String yourVersion;
            if (new File(targetPath + "version").exists()) {
                Scanner s = new Scanner(new File(targetPath + "version"));
                yourVersion = s.next();
            } else {
                yourVersion = "none";
            }
            URL website = new URL(projectUrl + "version");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(targetPath + "version");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            Scanner s = new Scanner(new File(targetPath + "version"));
            String currentVersion = s.next();
            if (currentVersion.equals(yourVersion)) {
                update = false;
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(LauncherForm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LauncherForm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LauncherForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (update) {

            try {
                if (new File(targetPath + "dist.zip").exists()) {
                    new File(targetPath + "dist.zip").delete();
                }
                DownloadTask dt = new DownloadTask(this, projectUrl + "dist.zip", targetPath);
                status = "Downloading update...";
                dt.doInBackground();
                status = "Unpacking update...";
                UnZip.unzip(targetPath + "dist.zip", targetPath);
                new File(targetPath + "dist.zip").delete();

            } catch (Exception ex) {
                Logger.getLogger(LauncherForm.class.getName()).log(Level.SEVERE, null, ex);
                status = "Error: " + ex;
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }
                System.exit(0);
            }
        }
        status = "Starting...";
        System.out.println(System.getProperty("java.home") + "/bin/java");
        Runtime.getRuntime().exec(System.getProperty("java.home") + "/bin/java -jar " + new File(targetPath + "core.jar").getAbsolutePath(), null, new File(targetPath));
        System.exit(0);
    }

    public void clicked(java.awt.event.MouseEvent evt) {
        if (evt.getX() < 20 && evt.getY() < 20) {
            System.exit(0);
        }
    }
    Image background;
    float loaded = 1;

    @Override
    public void paint(Graphics graphics) {
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = ((Graphics2D) bi.getGraphics());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.black);
        FontMetrics fm = new FontMetrics(g.getFont()) {
        };

        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        g.setColor(Color.red);
        drawButton(0, 0, 10, 20, g);
        g.setColor(Color.black);
        drawButton(20, 160, 250, 24, g);
        g.setColor(Color.white);
        g.drawString("X   " + title, 6, 15);
        g.drawString(status, 22, 156);
        drawButton(22, 162, 10 + (int) ((loaded) * 236f), 20, g);
        graphics.drawImage(bi, 0, 0, this);

    }

    public void drawButton(int x, int y, int w, int h, Graphics2D g) {
        g.fillRect(x, y, w, h - 10);
        g.fillOval(x, y + h - 20, 20, 20);
        g.fillOval(x + w - 10, y, 20, 20);
        g.fillRect(x + 10, y + 10, w, h - 10);
    }

    String status = "Checking updates...";

    void setFileInfo(String fileName, int contentLength) {
        status = Integer.valueOf(contentLength) + " bytes";
    }

}
