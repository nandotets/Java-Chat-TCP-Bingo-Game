/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Tipos.tipoCliente;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
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
    private int port;
    private Boolean exitFlag = false;
    private static JSONObject jsonSend = new JSONObject();
    private static JSONObject jsonReceived = new JSONObject();
    public static ArrayList<tipoCliente> listaClientes = new ArrayList<>();
    private DefaultListModel modelList = new DefaultListModel();

    /**
     * Creates new form Client
     */
    public Client() {
        initComponents();
        this.setLocationRelativeTo(null);
        onlineClients.setModel(modelList);
        popupmenu.setLayout(new GridLayout(5, 5));
    }

    public void connectServer() {
        try {
            connection = new Socket(host, port);

            out = connection.getOutputStream();
            outWr = new OutputStreamWriter(out);
            buffWr = new BufferedWriter(outWr);

            Client.jsonSend.put("COD", "login");
            Client.jsonSend.put("NOME", username);
            buffWr.write(Client.jsonSend.toString() + "\r\n");
            buffWr.flush();
            System.out.println("SEND: " + Client.jsonSend.toString());
            Client.jsonSend.clear();
        } catch (IOException ioex) {
            JOptionPane.showMessageDialog(null, "Error connect to server... (" + host + ":" + port + ")", "ERROR", JOptionPane.ERROR_MESSAGE);
            configServer();
            connectServer();
        }
    }

    public void sendMsg(String message) {
        try {
            Client.jsonSend.put("COD", "chat");
            Client.jsonSend.put("STATUS", "broad");
            Client.jsonSend.put("NOME", username);
            Client.jsonSend.put("MSG", message);
            buffWr.write(Client.jsonSend.toString() + "\r\n");
            chatArea.append("→ " + message + "\r\n");
            setScrollMaximum();
            buffWr.flush();
            System.out.println("SEND: " + Client.jsonSend.toString());
            Client.jsonSend.clear();
        } catch (IOException ioex) {
            JOptionPane.showMessageDialog(null, "Error connect to server... (" + host + ":" + port + ")", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void receiveMsg() {
        try {
            InputStream in = this.connection.getInputStream();
            InputStreamReader inRd = new InputStreamReader(in);
            BufferedReader buffRd = new BufferedReader(inRd);
            while (true) {
                if (buffRd.ready()) {
                    Client.jsonReceived = (JSONObject) JSONValue.parse(buffRd.readLine());
                    System.out.println("RECEIVE: " + Client.jsonReceived.toString());
                    String msg = (String) Client.jsonReceived.get("COD");
                    switch (msg) {
                        case "rlogin":
                            //LOGIN
                            msg = (String) Client.jsonReceived.get("STATUS");
                            switch (msg) {
                                case "sucesso":
                                    msg = (String) Client.jsonReceived.get("MSG");
                                    break;
                                case "falha":
                                    msg = (String) Client.jsonReceived.get("MSG");
                                    break;
                            }
                            break;
                        case "rlogout":
                            //LOGOUT
                            msg = (String) Client.jsonReceived.get("STATUS");
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
                                    modelList.clear();
                                    msg = null;
                                    this.exitFlag = true;
                                    break;
                                case "falha":
                                    chatArea.append("VOCÊ FALHOU AO SAIR DO CHAT!\r\n");
                                    setScrollMaximum();
                                    break;
                            }
                            break;
                        case "chat":
                            //MSG DE CHAT
                            msg = (String) Client.jsonReceived.get("STATUS");
                            switch (msg) {
                                case "uni":
                                    //UNICAST
                                    msg = "(PRIVATE) " + (String) Client.jsonReceived.get("MSG");
                                    break;
                                case "broad":
                                    //BROADCAST
                                    msg = (String) Client.jsonReceived.get("MSG");
                                    break;
                            }
                            break;
                        case "lista":
                            //LISTA DE ONLINE
                            modelList.clear();
                            Client.listaClientes.clear();
                            JSONArray lista = (JSONArray) Client.jsonReceived.get("LISTACLIENTE");
                            for (Object obj : lista) {
                                JSONObject jsonobj = (JSONObject) obj;
                                tipoCliente clt = new tipoCliente((String) jsonobj.get("NOME"), (String) jsonobj.get("IP"));
                                Client.listaClientes.add(clt);
                            }
                            for (tipoCliente cliente : Client.listaClientes) {
                                modelList.addElement(cliente.getNome());
                            }
                            break;

                        default:
                            break;
                    }
                    if (msg != null && !msg.equals("lista")) {
                        chatArea.append(msg + "\r\n");
                        setScrollMaximum();
                    }
                }
            }
        } catch (IOException ioex) {
            JOptionPane.showMessageDialog(null, "Error connect to server... (" + host + ":" + port + ")", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void exit() {
        int confirm = JOptionPane.showConfirmDialog(null, "Confirm to exit?", "EXIT", JOptionPane.YES_NO_OPTION);
        if (confirm == 0) {
            try {
                Client.jsonSend.clear();
                Client.jsonSend.put("COD", "logout");
                Client.jsonSend.put("NOME", "" + this.username);
                buffWr.write(Client.jsonSend.toString() + "\r\n");
                buffWr.flush();
                System.out.println("SEND: " + Client.jsonSend.toString());
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
        popupmenu = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem15 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenuItem20 = new javax.swing.JMenuItem();
        jMenuItem21 = new javax.swing.JMenuItem();
        jMenuItem22 = new javax.swing.JMenuItem();
        jMenuItem23 = new javax.swing.JMenuItem();
        jMenuItem24 = new javax.swing.JMenuItem();
        jMenuItem25 = new javax.swing.JMenuItem();
        panel = new javax.swing.JPanel();
        lChat = new javax.swing.JLabel();
        inTXT = new javax.swing.JTextField();
        bSend = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        chatArea = new javax.swing.JTextArea();
        bEmotes = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        onlineClients = new javax.swing.JList();
        lUsers = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuSettings = new javax.swing.JMenuItem();
        menuExit = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();

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

        jMenuItem1.setText("☺");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem1);

        jMenuItem2.setText("☻");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem2);

        jMenuItem3.setText("♥");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem3);

        jMenuItem4.setText("♦");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem4);

        jMenuItem5.setText("♣");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem5);

        jMenuItem6.setText("♠");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem6);

        jMenuItem7.setText("•");
        jMenuItem7.setToolTipText("");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem7);

        jMenuItem8.setText("◘");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem8);

        jMenuItem9.setText("○");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem9);

        jMenuItem10.setText("◙");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem10);

        jMenuItem11.setText("♂");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem11);

        jMenuItem12.setText("♀");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem12);

        jMenuItem13.setText("♪");
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem13);

        jMenuItem14.setText("♫");
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem14);

        jMenuItem15.setText("☼");
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem15);

        jMenuItem16.setText("►");
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem16);

        jMenuItem17.setText("◄");
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem17);

        jMenuItem18.setText("▲");
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem18);

        jMenuItem19.setText("▼");
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem19);

        jMenuItem20.setText("↑");
        jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem20ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem20);

        jMenuItem21.setText("↓");
        jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem21ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem21);

        jMenuItem22.setText("→");
        jMenuItem22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem22ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem22);

        jMenuItem23.setText("←");
        jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem23ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem23);

        jMenuItem24.setText("↕");
        jMenuItem24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem24ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem24);

        jMenuItem25.setText("↔");
        jMenuItem25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem25ActionPerformed(evt);
            }
        });
        popupmenu.add(jMenuItem25);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("CHATeTs");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        lChat.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lChat.setText("CHAT");

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

        bEmotes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/icon.png"))); // NOI18N
        bEmotes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bEmotesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
            .addGroup(panelLayout.createSequentialGroup()
                .addGap(188, 188, 188)
                .addComponent(lChat)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inTXT, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bSend, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bEmotes, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addComponent(lChat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLayout.createSequentialGroup()
                        .addComponent(inTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(bSend))
                    .addComponent(bEmotes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(21, 21, 21))
        );

        onlineClients.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(onlineClients);

        lUsers.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lUsers.setText("Users Online");

        menuFile.setText("File");

        menuSettings.setText("Server info");
        menuSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSettingsActionPerformed(evt);
            }
        });
        menuFile.add(menuSettings);

        menuExit.setText("Exit");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        menuFile.add(menuExit);

        jMenuBar1.add(menuFile);

        menuHelp.setText("Help");
        jMenuBar1.add(menuHelp);

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
                        .addComponent(lUsers)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lUsers)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSettingsActionPerformed
        viewConfigServer();
    }//GEN-LAST:event_menuSettingsActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (this.exitFlag == false) {
            exit();
        } else {
            System.exit(1);
        }
    }//GEN-LAST:event_formWindowClosing

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
        if (this.exitFlag == false) {
            exit();
        } else {
            System.exit(1);
        }
    }//GEN-LAST:event_menuExitActionPerformed

    private void bSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSendActionPerformed
        sendMsg(inTXT.getText());
        inTXT.setText("");
    }//GEN-LAST:event_bSendActionPerformed

    private void inTXTKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inTXTKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            sendMsg(inTXT.getText());
            inTXT.setText("");
        }
    }//GEN-LAST:event_inTXTKeyPressed

    private void bEmotesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEmotesActionPerformed
        popupmenu.show(bEmotes, bEmotes.getWidth(), bEmotes.getHeight());
    }//GEN-LAST:event_bEmotesActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "☺");
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "☻");
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "♥");
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "♦");
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "♣");
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "♠");
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "•");
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "◘");
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "○");
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "◙");
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "♂");
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "♀");
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "♪");
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "♫");
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "☼");
    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "►");
    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "◄");
    }//GEN-LAST:event_jMenuItem17ActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "▲");
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "▼");
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "↑");
    }//GEN-LAST:event_jMenuItem20ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "↓");
    }//GEN-LAST:event_jMenuItem21ActionPerformed

    private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "→");
    }//GEN-LAST:event_jMenuItem22ActionPerformed

    private void jMenuItem23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem23ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "←");
    }//GEN-LAST:event_jMenuItem23ActionPerformed

    private void jMenuItem24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem24ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "↕");
    }//GEN-LAST:event_jMenuItem24ActionPerformed

    private void jMenuItem25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem25ActionPerformed
        String hist = inTXT.getText();
        inTXT.setText(hist + "↔");
    }//GEN-LAST:event_jMenuItem25ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            ServerSocket onlyOne = new ServerSocket(2019);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Já existe outro Client aberto nesta máquina!", "ERRO", JOptionPane.INFORMATION_MESSAGE);
            System.exit(1);
        }

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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>
        Client app = new Client();
        app.setVisible(true);
        app.setFocusable(false);
        app.configServer();
        app.connectServer();
        app.receiveMsg();

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bEmotes;
    private javax.swing.JButton bSend;
    private javax.swing.JTextArea chatArea;
    private javax.swing.JTextField inTXT;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem20;
    private javax.swing.JMenuItem jMenuItem21;
    private javax.swing.JMenuItem jMenuItem22;
    private javax.swing.JMenuItem jMenuItem23;
    private javax.swing.JMenuItem jMenuItem24;
    private javax.swing.JMenuItem jMenuItem25;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lChat;
    private javax.swing.JLabel lUsers;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenuItem menuSettings;
    private javax.swing.JList onlineClients;
    private javax.swing.JPanel panel;
    private javax.swing.JPopupMenu popupmenu;
    // End of variables declaration//GEN-END:variables
}
