package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.Connection;
import net.OperationConverter;
import net.Server;
import operations.AddOperation;

/**
 * Entry point and Graphical User Interface
 *
 * @author fazo
 */
public class GUI extends javax.swing.JFrame {

    private static GUI gui;
    
    private final DocumentManager dm;
    private Server server;
    private Connection c;
    private final SettingsUI serverSettings;
    private final ConnectUI connectUI;
    private final JFileChooser fc;

    public GUI() {
        initComponents();
        gui = this;
        setTitle("Text Editor");
        dm = new DocumentManager(textArea.getDocument());
        fc = new JFileChooser();
        serverSettings = new SettingsUI();
        connectUI = new ConnectUI(this);
        fc.setMultiSelectionEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ContainerPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        statusLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        importButton = new javax.swing.JMenuItem();
        exportButton = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        settingsButton = new javax.swing.JMenuItem();
        connectMenuItem = new javax.swing.JMenuItem();
        hostMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(400, 312));

        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(textArea);

        statusLabel.setText("Offline");

        javax.swing.GroupLayout ContainerPanelLayout = new javax.swing.GroupLayout(ContainerPanel);
        ContainerPanel.setLayout(ContainerPanelLayout);
        ContainerPanelLayout.setHorizontalGroup(
            ContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ContainerPanelLayout.setVerticalGroup(
            ContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ContainerPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel))
        );

        jMenu1.setText("File");

        jMenuItem2.setText("Wipe");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });
        jMenu1.add(importButton);

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        jMenu1.add(exportButton);

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Network");

        settingsButton.setText("Settings");
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsButtonActionPerformed(evt);
            }
        });
        jMenu2.add(settingsButton);

        connectMenuItem.setText("Connect");
        connectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(connectMenuItem);

        hostMenuItem.setText("Host");
        hostMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(hostMenuItem);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        dm.wipe();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void hostMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostMenuItemActionPerformed
        if (server == null) {
            textArea.setEditable(false);
            dm.setListen(false);
            server = new Server(serverSettings.getPort(), dm);
            server.start();
            textArea.setText("Hosting a server on port " + server.getPort() + ".\nConnect with another instance of the program to edit");
            statusLabel.setText("Hosting - Port " + server.getPort());
            hostMenuItem.setText("Stop Hosting");
        } else {
            server.stop();
            server = null;
            textArea.setEditable(true);
            textArea.setText("");
            statusLabel.setText("Offline");
            dm.setListen(true);
            hostMenuItem.setText("Host");
        }
    }//GEN-LAST:event_hostMenuItemActionPerformed

    private void connectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectMenuItemActionPerformed
        if (c == null) { // Is currently disconnected
            connectUI.setVisible(true);
            connectUI.requestFocus();
        } else { // Is currently connected
            dm.unlinkConnection();
            c.close();
            c = null;
            connectMenuItem.setText("Connect");
            statusLabel.setText("Offline");
        }
    }//GEN-LAST:event_connectMenuItemActionPerformed

    public void connect(String hostname, int port) {
        if (c != null) {
            c.close();
        }
        statusLabel.setText("Connecting");
        c = new Connection(hostname, port, dm);
        dm.linkConnection(c);
        if (c.isOnline()) {
            statusLabel.setText("Online - Connected to " + c.getAddress() + ":" + c.getPort());
            connectMenuItem.setText("Disconnect");
        } else {
            JOptionPane.showMessageDialog(this, "Connection failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        int ret = fc.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            String s;
            try {
                s = OperationConverter.load(f);
                dm.wipe();
                dm.apply(new AddOperation(0, s, null));
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Load Operation failed:\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_importButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        int ret = fc.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                OperationConverter.save(dm.getStack(), f);
                JOptionPane.showMessageDialog(this, "Document saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save Operation failed:\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsButtonActionPerformed
        serverSettings.setVisible(true);
        serverSettings.requestFocus();
    }//GEN-LAST:event_settingsButtonActionPerformed

    public static GUI get(){
        return gui;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            // Set Look and Feel to "System" which means that Swing will use the
            // OS's native GUI toolkit to render the application, making it look
            // native
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ContainerPanel;
    private javax.swing.JMenuItem connectMenuItem;
    private javax.swing.JMenuItem exportButton;
    private javax.swing.JMenuItem hostMenuItem;
    private javax.swing.JMenuItem importButton;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem settingsButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
