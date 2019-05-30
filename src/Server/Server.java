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

    public static ArrayList<tipoCliente> listaClientes = new ArrayList<>();
    private static ServerSocket server;
    private Socket connection;
    private InputStream in;
    private InputStreamReader inRead;
    private BufferedReader bufRead;
    private BufferedWriter buffWr;
    private Writer outWr;
    private OutputStream out;
    private static JTextArea ta = new JTextArea(250, 250);
    private static JFrame frame = new JFrame();
    private static JScrollPane jsp;
    private static JSONObject jsonSend = new JSONObject();
    private static JSONObject jsonReceived = new JSONObject();
    private static JSONArray onlineList = new JSONArray();

    public Server(Socket sClient) {
        this.connection = sClient;
        setScrollMaximum();
        try {
            in = sClient.getInputStream();
            inRead = new InputStreamReader(in);
            bufRead = new BufferedReader(inRead);
        } catch (Exception ioex) {
            System.err.println("Error: Server Constructor -> " + ioex);
        }
    }

    @Override
    public void run() {
        String msg = "";
        tipoCliente cliente = new tipoCliente("", this.connection.getInetAddress().getHostAddress(), String.valueOf(this.connection.getPort()));
        try {
            out = connection.getOutputStream();
            outWr = new OutputStreamWriter(out);
            buffWr = new BufferedWriter(outWr);

            while (msg != null) {
                msg = bufRead.readLine();
                Server.jsonReceived = (JSONObject) JSONValue.parse(msg);
                ta.append("RECEIVE: " + msg + "\r\n");
                msg = (String) Server.jsonReceived.get("COD");
                switch (msg) {
                    case "login":
                        //LOGIN
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) Server.jsonReceived.get("NOME"));
                        Server.listaClientes.add(cliente);
                        Server.jsonSend.clear();
                        Server.jsonSend.put("COD", "rlogin");
                        Server.jsonSend.put("STATUS", "sucesso");
                        cliente.getBuffWr().write(Server.jsonSend.toString() + "\r\n");
                        cliente.getBuffWr().flush();
                        ta.append("SEND: " + Server.jsonSend.toString() + "\r\n");
                        Server.jsonSend.clear();
                        Server.jsonSend.put("COD", "chat");
                        Server.jsonSend.put("STATUS", "broad");
                        Server.jsonSend.put("MSG", "Seja bem-vindo ao CHATeTs " + cliente.getNome() + "!");
                        cliente.getBuffWr().write(Server.jsonSend.toString() + "\r\n");
                        cliente.getBuffWr().flush();
                        ta.append("SEND: " + Server.jsonSend.toString() + "\r\n");
                        ta.append("--->" + cliente.getNome() + "(" + cliente.getIp() + ")" + " connected!\r\n");
                        setScrollMaximum();
                        broadcastMsg(cliente.getBuffWr());
                        sendOnlineListBroadcast();
                        break;
                    case "logout":
                        //LOGOUT
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) Server.jsonReceived.get("NOME"));
                        cliente.setIp(this.connection.getInetAddress().getHostAddress());
                        cliente.setPorta(String.valueOf(this.connection.getPort()));
                        if (Server.listaClientes.remove(cliente)) {
                            sendOnlineListBroadcast();
                            Server.jsonSend.clear();
                            Server.jsonSend.put("COD", "chat");
                            Server.jsonSend.put("STATUS", "broad");
                            Server.jsonSend.put("MSG", cliente.getNome() + " se desconectou do CHATeTs...");
                            broadcastMsg(cliente.getBuffWr());
                            Server.jsonSend.put("COD", "rlogout");
                            Server.jsonSend.put("STATUS", "sucesso");
                            ta.append("SEND: " + Server.jsonSend.toString() + "\r\n");
                            ta.append("--> " + cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!\r\n");
                            cliente.getBuffWr().write(Server.jsonSend.toString() + "\r\n");
                            cliente.getBuffWr().flush();

                        } else {
                            Server.jsonSend.put("COD", "rlogout");
                            Server.jsonSend.put("STATUS", "falha");
                            ta.append("SEND: " + Server.jsonSend.toString() + "\r\n");
                            cliente.getBuffWr().write(Server.jsonSend.toString() + "\r\n");
                            cliente.getBuffWr().flush();
                        }
                        setScrollMaximum();
                        break;
                    case "chat":
                        //CHAT
                        msg = (String) Server.jsonReceived.get("STATUS");
                        switch (msg) {
                            case "uni":
                                msg = (String) Server.jsonReceived.get("MSG");
                                Server.jsonSend.clear();
                                Server.jsonSend.put("NOME", (String) Server.jsonReceived.get("NOME"));
                                Server.jsonSend.put("MSG", "(PRIVATE FROM " + cliente.getNome() + ") → " + msg);
                                Server.jsonSend.put("STATUS", "uni");
                                JSONArray arr = (JSONArray) Server.jsonReceived.get("LISTACLIENTE");
                                JSONObject cDestino = (JSONObject) arr.get(0);
                                unicastMsg(cDestino);
                                break;
                            case "broad":
                                msg = (String) Server.jsonReceived.get("MSG");
                                Server.jsonSend.clear();
                                Server.jsonSend.put("COD", "chat");
                                Server.jsonSend.put("STATUS", "broad");
                                Server.jsonSend.put("MSG", Server.jsonReceived.get("NOME") + " → " + msg);
                                broadcastMsg(cliente.getBuffWr());
                                Server.jsonSend.clear();
                                System.out.println(Server.jsonReceived.get("NOME") + " → " + msg);
                                setScrollMaximum();
                                break;
                        }
                        break;
                }
            }

        } catch (Exception ex) {
            if (Server.listaClientes.remove(cliente)) {
                Server.jsonSend.clear();
                Server.jsonSend.put("COD", "chat");
                Server.jsonSend.put("STATUS", "broad");
                Server.jsonSend.put("MSG", cliente.getNome() + " se desconectou do CHATeTs...");
                broadcastMsg(cliente.getBuffWr());
                ta.append("--> " + cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!\r\n");
            }
            sendOnlineListBroadcast();
            setScrollMaximum();
        }
    }

    public void broadcastMsg(BufferedWriter bufWrOUT) {
        BufferedWriter bufWrAUX;
        String msg = (String) Server.jsonSend.get("COD");
        JSONArray jsonArr = new JSONArray();
        try {
            for (tipoCliente clients : Server.listaClientes) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                if (!(bufWrOUT == bufWrAUX)) {
                    ta.append("SEND TO '" + clients.getNome() + "': " + Server.jsonSend.toString() + "\r\n");
                    bufWrAUX.write(Server.jsonSend.toString() + "\r\n");
                    bufWrAUX.flush();
                }
            }
            jsonArr.clear();
        } catch (Exception ex) {
            ta.append("Error to send broadcast message...\r\n");
            System.err.println("Error to send broadcast message: " + ex);
            setScrollMaximum();
        }
        Server.jsonSend.clear();
    }

    public void sendOnlineListBroadcast() {
        BufferedWriter bufWrAUX;
        JSONArray jsonArr = new JSONArray();
        Server.jsonSend.clear();
        Server.jsonSend.put("COD", "lista");
        try {
            for (tipoCliente clt : Server.listaClientes) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("NOME", clt.getNome());
                jsonObj.put("IP", clt.getIp());
                jsonObj.put("PORTA", clt.getPorta());
                jsonArr.add(jsonObj);
            }
            Server.jsonSend.put("LISTACLIENTE", jsonArr);

            for (tipoCliente clients : Server.listaClientes) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                ta.append("SEND TO '" + clients.getNome() + "': " + Server.jsonSend.toString() + "\r\n");
                bufWrAUX.write(Server.jsonSend.toString() + "\r\n");
                bufWrAUX.flush();
            }
            jsonArr.clear();
            Server.jsonSend.clear();
        } catch (Exception ex) {
            ta.append("Error to send online list broadcast ...\r\n");
            System.err.println("Error to send OnlineListBroadcast: " + ex);
            setScrollMaximum();
        }
    }

    public void unicastMsg(JSONObject cDestino) {
        try {
            for (tipoCliente clients : Server.listaClientes) {
                String nome = clients.getNome();
                String ip = clients.getIp();
                if (nome.equals(cDestino.get("NOME").toString()) && ip.equals(cDestino.get("IP").toString())) {
                    ta.append("SEND TO '" + clients.getNome() + "': " + Server.jsonSend.toString() + "\r\n");
                    clients.getBuffWr().write(Server.jsonSend.toString() + "\r\n");
                    clients.getBuffWr().flush();
                    break;
                }
            }
            Server.jsonSend.clear();
        } catch (Exception ex) {
            ta.append("Error to send unicast message...\r\n");
            System.err.println("Error to send unicast message: " + ex);
            setScrollMaximum();
        }
    }

    private static int configPort() {
        JTextField field1 = new JTextField();
        Object[] message = {
            "Server PORT:", field1
        };

        field1.setText("123");
        int option = JOptionPane.showConfirmDialog(null, message, "SERVER CONFIG", JOptionPane.OK_CANCEL_OPTION);
        if (option == 0) {
            try {
                return Integer.parseInt(field1.getText());
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
        return 0;
    }

    private static void initComponents(int port) throws UnknownHostException {
        frame = new JFrame("CHATeTs Server. PORT:" + port);
        ta = new JTextArea(30, 50);
        ta.setEditable(false);
        ta.setLineWrap(true);
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        jsp = new JScrollPane(ta);
        frame.getContentPane().add(jsp);
        ta.append("Server opened! " + InetAddress.getLocalHost().getHostAddress() + ":" + port + "\r\n");
        setScrollMaximum();
        frame.setVisible(true);
    }

    private static void setScrollMaximum() {
        JScrollBar x = jsp.getVerticalScrollBar();
        x.setValue(x.getMaximum());
        jsp.setVerticalScrollBar(x);
    }

    public static void main(String[] args) {
        try {
            int portConfig = configPort();
            server = new ServerSocket(portConfig);
            initComponents(portConfig);
            while (true) { //Mantem servidor ativo mesmo com desconexões de clientes
                Socket clientC = server.accept();
                Thread t = new Server(clientC);
                t.start();
            }
        } catch (IOException ioex) {
            System.err.println("Exception Server main()" + ioex);
        } catch (IllegalArgumentException argex) {
            System.err.println("Invalid PORT!");
        }
    }
}
