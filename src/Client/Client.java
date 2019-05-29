/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Tipos.tipoCliente;
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
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author Fernando
 */
public class Client extends javax.swing.JFrame {

    private Socket connection;
    private OutputStream out;
    private Writer outWr;
    private BufferedWriter buffWr;
    private String host, username;
    private int port = 0;
    private static JSONObject jsonobjSend = new JSONObject();
    private static JSONObject jsonobjReceive = new JSONObject();
    private ArrayList<tipoCliente> listaClientes = new ArrayList<>();
    private DefaultListModel modelList = new DefaultListModel();

    /**
     * Creates new form Client
     */
    public Client() {
        initComponents();
        this.setLocationRelativeTo(null);
        onlineClients.setModel(modelList);
    }

    public void connectServer() {
        try {
            connection = new Socket(host, port);

            out = connection.getOutputStream();
            outWr = new OutputStreamWriter(out);
            buffWr = new BufferedWriter(outWr);

            jsonobjSend.put("COD", "login");
            jsonobjSend.put("NOME", "" + username);
            //System.out.println(jsonobjSend.toString()+"\r\n");
            buffWr.write(jsonobjSend.toString() + "\r\n");
            buffWr.flush();
            jsonobjSend.clear();
        } catch (IOException ioex) {
            JOptionPane.showMessageDialog(null, "Error connect to server... (" + host + ":" + port + ")", "ERROR", JOptionPane.ERROR_MESSAGE);
            configServer();
            connectServer();
        }
    }

    public void sendMsg(String msg) {
        try {
            jsonobjSend.put("COD", "chat");
            jsonobjSend.put("STATUS", "broad");
            jsonobjSend.put("NOME", username);
            jsonobjSend.put("MSG", msg);
            buffWr.write(jsonobjSend.toString() + "\r\n");
            System.out.println("SEND: " + jsonobjSend.toString());
            chatArea.append("→ " + msg + "\r\n");
            setScrollMaximum();
            buffWr.flush();
            jsonobjSend.clear();
        } catch (IOException ioex) {
            JOptionPane.showMessageDialog(null, "Error connect to server... (" + host + ":" + port + ")", "ERROR", JOptionPane.ERROR_MESSAGE);
            //System.err.println("Error Send Msg: " + ioex);
        }
    }

    public void receiveMsg() {
        try {
            InputStream in = connection.getInputStream();
            InputStreamReader inRd = new InputStreamReader(in);
            BufferedReader bufRd = new BufferedReader(inRd);
            while (true) {
                if (bufRd.ready()) {
                    jsonobjReceive = (JSONObject) JSONValue.parse(bufRd.readLine());
                    System.out.println("RECEIVE: " + jsonobjReceive.toString());
                    String msg = (String) jsonobjReceive.get("COD");
                    switch (msg) {
                        case "rlogin":
                            //LOGIN
                            msg = (String) jsonobjReceive.get("STATUS");
                            switch (msg) {
                                case "sucesso":
                                    msg = (String) jsonobjReceive.get("MSG");
                                    break;
                                case "falha":
                                    msg = (String) jsonobjReceive.get("MSG");
                                    break;
                            }
                            break;
                        case "rlogout":
                            //LOGOUT
                            msg = (String) jsonobjReceive.get("STATUS");
                            switch (msg) {
                                case "sucesso":
                                    chatArea.append("Você saiu do chat...\r\n");
                                    setScrollMaximum();
                                    buffWr.close();
                                    outWr.close();
                                    out.close();
                                    connection.close();
                                    inTXT.setEnabled(false);
                                    bSend.setEnabled(false);
                                    onlineClients.removeAll();
                                    break;
                                case "falha":
                                    chatArea.append("VOCÊ FALHOU AO SAIR DO CHAT!\r\n");
                                    setScrollMaximum();
                                    break;
                            }
                            break;
                        case "chat":
                            //MSG DE CHAT
                            msg = (String) jsonobjReceive.get("STATUS");
                            switch (msg) {
                                case "uni":
                                    //UNICAST
                                    msg = "(PRIVATE) " + (String) jsonobjReceive.get("MSG");
                                    break;
                                case "broad":
                                    //BROADCAST
                                    msg = (String) jsonobjReceive.get("MSG");
                                    break;
                            }
                            break;

                        case "lista":
                            //LISTA DE ONLINE
                            JSONArray lista = (JSONArray) jsonobjReceive.get("LISTACLIENTE");
                            for (Object obj : lista) {
                                JSONObject jsonobj = (JSONObject) obj;
                                tipoCliente clt = new tipoCliente((String) jsonobj.get("NOME"), (String) jsonobj.get("IP"));
                                listaClientes.add(clt);
                            }
                            modelList.clear();
                            for (tipoCliente cliente : listaClientes) {
                                modelList.addElement(cliente.getNome());
                            }
                            break;

                        default:
                            break;
                    }
                    if (msg != null && msg != "lista") {
                        chatArea.append(msg + "\r\n");
                        setScrollMaximum();
                    }
                }
                jsonobjReceive.clear();
            }
        } catch (IOException ioex) {
            JOptionPane.showMessageDialog(null, "Error connect to server... (" + host + ":" + port + ")", "ERROR", JOptionPane.ERROR_MESSAGE);
            //System.err.println("Error Receive Msg: " + ioex);
        }
    }

    public void exit() {
        int confirm = JOptionPane.showConfirmDialog(null, "Confirm to exit?", "EXIT", JOptionPane.YES_NO_OPTION);
        if (confirm == 0) {
            try {
                jsonobjSend.put("COD", "logout");
                jsonobjSend.put("NOME", "" + this.username);
                buffWr.write(jsonobjSend.toString() + "\r\n");
                System.out.println("SEND: " + jsonobjSend.toString());
            } catch (Exception ioex) {
                JOptionPane.showMessageDialog(null, "Error to exit...", "ERROR", JOptionPane.ERROR_MESSAGE);
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
        jLabel1 = new javax.swing.JLabel();
        inTXT = new javax.swing.JTextField();
        bSend = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        chatArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        onlineClients = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuExit = new javax.swing.JMenu();
        menuSettings = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
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
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel1.setText("CHAT");

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

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(inTXT)
            .addComponent(jScrollPane3)
            .addGroup(panelLayout.createSequentialGroup()
                .addGap(188, 188, 188)
                .addComponent(jLabel1)
                .addContainerGap(198, Short.MAX_VALUE))
            .addComponent(bSend, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bSend)
                .addGap(23, 23, 23))
        );

        onlineClients.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(onlineClients);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("Users Online");

        menuExit.setText("File");

        menuSettings.setText("Server info");
        menuSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSettingsActionPerformed(evt);
            }
        });
        menuExit.add(menuSettings);

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        menuExit.add(jMenuItem1);

        jMenuBar1.add(menuExit);

        jMenu2.setText("Help");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel2)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2)
                        .addGap(20, 20, 20))))
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

    private void menuSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSettingsActionPerformed
        viewConfigServer();
    }//GEN-LAST:event_menuSettingsActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        exit();
    }//GEN-LAST:event_formWindowClosing

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        exit();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

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
    private javax.swing.JButton bSend;
    private javax.swing.JTextArea chatArea;
    private javax.swing.JTextField inTXT;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JMenu menuExit;
    private javax.swing.JMenuItem menuSettings;
    private javax.swing.JList onlineClients;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables
}
