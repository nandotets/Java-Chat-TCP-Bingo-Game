/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chattcp;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JTextField;

/**
 *
 * @author Fernando
 */
public class Client extends javax.swing.JFrame {

    private Socket connection;
    private OutputStream out;
    private Writer outWr;
    private BufferedWriter bufWr;
    private String host, username;
    private int port = 0;

    /**
     * Creates new form Client
     */
    public Client() {
        initComponents();
        this.setLocationRelativeTo(null);

    }

    public void connectServer() {
        try {
            connection = new Socket(host, port);

            out = connection.getOutputStream();
            outWr = new OutputStreamWriter(out);
            bufWr = new BufferedWriter(outWr);

            bufWr.write(username + "\r\n");
            bufWr.flush();

        } catch (IOException ioex) {
            JOptionPane.showMessageDialog(null, "Error connect to server... (" + host + ":" + port + ")", "ERROR", JOptionPane.ERROR_MESSAGE);
            configServer();
            connectServer();
        }
        chatArea.append("• Welcome " + username + "! •\r\n");
        setScrollMaximum();
    }

    public void sendMsg(String msg) {
        try {
            bufWr.write(msg + "\r\n");
            chatArea.append("→ " + inTXT.getText() + "\r\n");
            setScrollMaximum();
            bufWr.flush();
        } catch (IOException ioex) {
            System.err.println("Error Send Msg: " + ioex);
        }
    }

    public void receiveMsg() {
        try {
            InputStream in = connection.getInputStream();
            InputStreamReader inRd = new InputStreamReader(in);
            BufferedReader bufRd = new BufferedReader(inRd);
            String msg;
            while (true) {
                if (bufRd.ready()) {
                    msg = bufRd.readLine();
                    chatArea.append(msg + "\r\n");
                    setScrollMaximum();
                }
            }
        } catch (IOException ioex) {
            System.err.println("Error Receive Msg: " + ioex);
        }
    }

    public void exit() {
        int confirm = JOptionPane.showConfirmDialog(null, "Confirm to exit?", "EXIT", JOptionPane.YES_NO_OPTION);
        if (confirm == 0) {
            try {
                bufWr.write("EXIT\r\n");
                bufWr.close();
                outWr.close();
                out.close();
                connection.close();
                System.exit(1);
            } catch (Exception ioex) {
                System.err.println("Error to exit: " + ioex);
                System.exit(1);
            }
        }
    }

    public void configServer() {
        JTextField field1 = new JTextField();
        JTextField field2 = new JTextField();
        JTextField field3 = new JTextField();

        if (username != null) {
            field3.setText(this.username);
        }
        if (host != null) {
            field1.setText(this.host);
        }
        if (port != 0) {
            field2.setText("" + this.port);
        }

        field1.setText("localhost");
        field2.setText("12345");
        field3.setText("XABLAU");

        Object[] message = {
            "Server IP:", field1,
            "Server PORT:", field2,
            "Your Username:", field3
        };

        int option = JOptionPane.showConfirmDialog(null, message, "SERVER CONNECTION SETTINGS", JOptionPane.OK_CANCEL_OPTION);

        if (option == 0) {
            try {
                username = field3.getText();
                host = field1.getText();
                if (field1.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Invalid Server IP!", "ERROR SERVER IP", JOptionPane.ERROR_MESSAGE);
                    configServer();
                } else if (field3.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Invalid Username!", "ERROR INVALID USERNAME", JOptionPane.ERROR_MESSAGE);
                    configServer();
                }
                port = Integer.parseInt(field2.getText());
            } catch (NumberFormatException nbex) {
                JOptionPane.showMessageDialog(null, "Invalid Server PORT!", "ERROR SERVER PORT", JOptionPane.ERROR_MESSAGE);
                configServer();
            }
        } else if (option == -1) {
            int confirm = JOptionPane.showConfirmDialog(null, "The CHAT will close. Confirm?", "EXIT", JOptionPane.YES_NO_OPTION);
            if (confirm == 0) {
                System.exit(1);
            }
        }
    }

    public void viewConfigServer() {
        JTextField field1 = new JTextField();
        JTextField field2 = new JTextField();
        JTextField field3 = new JTextField();

        field1.setText(this.host);
        field2.setText("" + this.port);
        field3.setText(this.username);

        field1.setEnabled(false);
        field2.setEnabled(false);
        field3.setEnabled(false);

        Object[] message = {
            "Server IP:", field1,
            "Server PORT:", field2,
            "Your Username:", field3
        };

        JOptionPane.showMessageDialog(null, message, "SERVER CONNECTION SETTINGS", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setScrollMaximum() {
        JScrollBar x = jScrollPane2.getVerticalScrollBar();
        x.setValue(x.getMaximum());
        jScrollPane2.setVerticalScrollBar(x);

        JScrollBar y = jScrollPane3.getVerticalScrollBar();
        y.setValue(y.getMaximum());
        jScrollPane3.setVerticalScrollBar(y);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        panel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        onlineArea = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        inTXT = new javax.swing.JTextField();
        bSend = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        chatArea = new javax.swing.JTextArea();
        bExit = new javax.swing.JToggleButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuSettings = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CHATeTs");

        onlineArea.setEditable(false);
        jScrollPane2.setViewportView(onlineArea);

        jLabel1.setText("CHAT");

        jLabel2.setText("Users Online");

        inTXT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                inTXTKeyPressed(evt);
            }
        });

        bSend.setText("SEND");
        bSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSendActionPerformed(evt);
            }
        });

        chatArea.setEditable(false);
        chatArea.setColumns(20);
        chatArea.setLineWrap(true);
        chatArea.setRows(5);
        jScrollPane3.setViewportView(chatArea);

        bExit.setText("EXIT");
        bExit.setFocusPainted(false);
        bExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addGap(119, 119, 119)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(24, 24, 24))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelLayout.createSequentialGroup()
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelLayout.createSequentialGroup()
                        .addGap(156, 156, 156)
                        .addComponent(bExit, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 43, Short.MAX_VALUE))
                    .addComponent(jScrollPane3)
                    .addComponent(inTXT, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .addComponent(bSend, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bSend))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bExit)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jMenu1.setText("File");

        menuSettings.setText("Server info");
        menuSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSettingsActionPerformed(evt);
            }
        });
        jMenu1.add(menuSettings);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Help");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void inTXTKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inTXTKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            sendMsg(inTXT.getText());
            inTXT.setText("");
        }
    }//GEN-LAST:event_inTXTKeyPressed

    private void bSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSendActionPerformed
        sendMsg(inTXT.getText());
        inTXT.setText("");
    }//GEN-LAST:event_bSendActionPerformed

    private void bExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bExitActionPerformed
        exit();
    }//GEN-LAST:event_bExitActionPerformed

    private void menuSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSettingsActionPerformed
        viewConfigServer();
    }//GEN-LAST:event_menuSettingsActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Client.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        Client app = new Client();
        app.setVisible(true);
        app.setFocusable(false);
        app.configServer();
        app.connectServer();
        app.receiveMsg();

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton bExit;
    private javax.swing.JButton bSend;
    private javax.swing.JTextArea chatArea;
    private javax.swing.JTextField inTXT;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JMenuItem menuSettings;
    private javax.swing.JTextPane onlineArea;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables
}
