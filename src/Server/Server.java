/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Tipos.tipoCliente;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author Fernando
 */
public class Server extends Thread {

    private static ArrayList<tipoCliente> listaClientes = new ArrayList<>();
    private static ServerSocket server;
    private static String username, host, clientIP;
    private Socket connection;
    private InputStream in;
    private InputStreamReader inRead;
    private BufferedReader bufRead;
    private static int port;
    private BufferedWriter buffWr;
    private Writer outWr;
    private OutputStream out;
    private static JTextArea ta = new JTextArea(250, 250);
    private static JFrame frame = new JFrame();
    private static JScrollPane jsp;
    private static JSONObject jsonobjSend = new JSONObject();
    private static JSONObject jsonobjReceive = new JSONObject();
    private static JSONArray list = new JSONArray();

    public Server(Socket sClient) {
        this.connection = sClient;
        setScrollMaximum();
        try {
            in = sClient.getInputStream();
            inRead = new InputStreamReader(in);
            bufRead = new BufferedReader(inRead);
        } catch (Exception ioex) {
            JOptionPane.showMessageDialog(null, "Exception Server Constructor", "ERROR SERVER", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void run() {
        String msg = "";
        tipoCliente cliente = new tipoCliente(username, clientIP);
        try {
            out = connection.getOutputStream();
            outWr = new OutputStreamWriter(out);
            buffWr = new BufferedWriter(outWr);
            cliente.setBuffWr(buffWr);
            while (msg != null) {
                msg = bufRead.readLine();
                jsonobjReceive = (JSONObject) JSONValue.parse(msg);
                System.out.println(msg);
                msg = (String) jsonobjReceive.get("COD");
                switch (msg) {
                    case "login":
                        //LOGIN
                        username = (String) jsonobjReceive.get("NOME");
                        cliente.setNome(username);
                        listaClientes.add(cliente);
                        jsonobjSend.put("COD", "rlogin");
                        jsonobjSend.put("STATUS", "sucesso");
                        buffWr.write(jsonobjSend.toString() + "\r\n");
                        buffWr.flush();
                        jsonobjSend.put("COD", "chat");
                        jsonobjSend.put("STATUS", "broad");
                        jsonobjSend.put("MSG", "Seja bem-vindo ao CHATeTs " + username + "!");
                        buffWr.write(jsonobjSend.toString() + "\r\n");
                        buffWr.flush();
                        ta.append("** " + cliente.getNome() + "(" + cliente.getIp() + ")" + " connected!\r\n");
                        setScrollMaximum();
                        broadcastMsg(listaClientes, buffWr, jsonobjSend);
                        jsonobjSend.clear();
                        jsonobjSend.put("COD", "lista");
                        broadcastMsg(listaClientes, buffWr, jsonobjSend);
                        break;
                    case "logout":
                        //LOGOUT
                        listaClientes.remove(cliente);
                        jsonobjSend.put("COD", "rlogout");
                        jsonobjSend.put("STATUS", "sucesso");
                        buffWr.write(jsonobjSend.toString() + "\r\n");
                        buffWr.flush();
                        jsonobjSend.put("COD", "chat");
                        jsonobjSend.put("STATUS", "broad");
                        jsonobjSend.put("MSG", cliente.getNome() + " se desconectou do CHATeTs...");
                        broadcastMsg(listaClientes, buffWr, jsonobjSend);
                        ta.append("** " + cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!\r\n");
                        setScrollMaximum();
                        jsonobjSend.clear();
                        jsonobjSend.put("COD", "lista");
                        broadcastMsg(listaClientes, buffWr, jsonobjSend);
                        break;
                    case "chat":
                        //CHAT
                        msg = (String) jsonobjReceive.get("STATUS");
                        switch (msg) {
                            case "uni":
                                msg = (String) jsonobjReceive.get("MSG");
                                username = (String) jsonobjReceive.get("NOME");
                                jsonobjSend.put("MSG", "(PRIVATE) " + username + " → " + msg);
                                list = (JSONArray) jsonobjReceive.get("LISTACLIENTE");
                                jsonobjSend.put("LISTACLIENTES", list);
                                unicastMsg(listaClientes, jsonobjSend);
                                break;
                            case "broad":
                                msg = (String) jsonobjReceive.get("MSG");
                                username = (String) jsonobjReceive.get("NOME");
                                jsonobjSend.put("MSG", username + " → " + msg);
                                broadcastMsg(listaClientes, buffWr, jsonobjSend);
                                ta.append(username + " → " + msg + "\r\n");
                                setScrollMaximum();
                                break;
                        }
                        break;
                }
                jsonobjSend.clear();
                jsonobjReceive.clear();
            }

        } catch (Exception ex) {
            listaClientes.remove(cliente);
            jsonobjSend.put("COD", "rlogout");
            jsonobjSend.put("STATUS", "sucesso");
            jsonobjSend.put("MSG", cliente.getNome() + " se desconectou do CHATeTs...");
            ta.append("** " + cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!\r\n");
            setScrollMaximum();
            broadcastMsg(listaClientes, buffWr, jsonobjSend);
            jsonobjSend.clear();
            jsonobjSend.put("COD", "lista");
            broadcastMsg(listaClientes, buffWr, jsonobjSend);
            jsonobjSend.clear();
        }
    }

    public void broadcastMsg(ArrayList<tipoCliente> listaClientes, BufferedWriter bufWrOUT, JSONObject jsonSend) {
        BufferedWriter bufWrAUX;
        String msg;
        JSONObject jsonObj = new JSONObject();
        JSONArray jsonArr = new JSONArray();
        try {
            for (tipoCliente clt : listaClientes) {
                bufWrAUX = (BufferedWriter) clt.getBuffWr();
                if (bufWrOUT != bufWrAUX) {
                    msg = (String) jsonSend.get("COD");
                    if (msg.equals("lista")) {
                        for (tipoCliente list : listaClientes) {
                            jsonObj.put("NOME", list.getNome());
                            jsonObj.put("IP", list.getIp());
                            jsonArr.add(jsonObj);
                        }
                        jsonSend.put("LISTACLIENTE", jsonArr);
                        System.out.println(jsonSend.toString());
                    }
                    System.out.println(jsonSend.toString());
                    bufWrAUX.write(jsonSend.toString() + "\r\n");
                    bufWrAUX.flush();
                }
            }
        } catch (Exception ex) {
            ta.append("Error to send broadcast message...\r\n");
            setScrollMaximum();
        }
    }

    public void unicastMsg(ArrayList<tipoCliente> listaClientes, JSONObject jsonSend) {
        try {
            BufferedWriter bufWrAUX;
            list = (JSONArray) jsonSend.get("LISTACLIENTES");
            JSONObject o = (JSONObject) list.get(0);

            for (tipoCliente clt : listaClientes) {
                if (clt.getIp().equals(o.get("IP"))) {
                    bufWrAUX = (BufferedWriter) clt.getBuffWr();
                    System.out.println(jsonSend.toString());
                    bufWrAUX.write(jsonSend.toString() + "\r\n");
                    bufWrAUX.flush();
                }
            }
        } catch (Exception ex) {
            ta.append("Error to send broadcast message...\r\n");
            setScrollMaximum();
        }
    }

    private static void configPort() {
        JTextField field1 = new JTextField();
        Object[] message = {
            "Server PORT:", field1
        };
        int option = JOptionPane.showConfirmDialog(null, message, "SERVER CONFIG", JOptionPane.OK_CANCEL_OPTION);
        System.out.println(option);
        if (option == 0) {
            try {
                port = Integer.parseInt(field1.getText());
            } catch (NumberFormatException nbex) {
                JOptionPane.showMessageDialog(null, "Invalid Server PORT!", "ERROR SERVER PORT", JOptionPane.ERROR_MESSAGE);
                configPort();
            }
        } else if (option == -1 || option == 2) {
            int confirm = JOptionPane.showConfirmDialog(null, "Cancel to open server?", "EXIT", JOptionPane.YES_NO_OPTION);
            if (confirm == 0) {
                System.exit(1);
            }
        }
    }

    private static void initComponents() {
        frame = new JFrame("CHATeTs Server. PORT:" + port);
        ta = new JTextArea(20, 30);
        ta.setEditable(false);
        ta.setLineWrap(true);
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        jsp = new JScrollPane(ta);
        frame.getContentPane().add(jsp);
        ta.append("Server opened! " + host + ":" + port + "\r\n");
        setScrollMaximum();
        frame.setVisible(true);
    }

    private static void setScrollMaximum() {
        JScrollBar x = jsp.getVerticalScrollBar();
        x.setValue(x.getMaximum());
        jsp.setVerticalScrollBar(x);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            configPort();
            host = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Opening server... " + host + ":" + port);
            server = new ServerSocket(port);
            System.out.println("Server opened! " + host + ":" + port);
            initComponents();
            while (true) { //Mantem servidor ativo mesmo com desconexões de clientes
                Socket clientC = server.accept();
                clientIP = clientC.getInetAddress().getHostAddress();
                Thread t = new Server(clientC);
                t.start();
            }
        } catch (UnknownHostException unkex) {
            System.err.println("Invalid host!");
        } catch (IOException ioex) {
            System.err.println("Exception Server main()" + ioex);
        } catch (IllegalArgumentException argex) {
            System.err.println("Invalid PORT!");
        }
    }
}
