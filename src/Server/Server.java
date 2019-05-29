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
    private static JSONObject jsonobjSend = new JSONObject();
    private static JSONObject jsonReceived = new JSONObject();
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
        tipoCliente cliente = new tipoCliente("", this.connection.getInetAddress().getHostAddress());
        try {
            out = connection.getOutputStream();
            outWr = new OutputStreamWriter(out);
            buffWr = new BufferedWriter(outWr);

            while (msg != null) {
                msg = bufRead.readLine();
                jsonReceived = (JSONObject) JSONValue.parse(msg);
                System.out.println("RECEIVE: " + msg);
                msg = (String) jsonReceived.get("COD");
                switch (msg) {
                    case "login":
                        //LOGIN
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        Server.listaClientes.add(cliente);
                        for (tipoCliente listaCliente : listaClientes) {
                            System.out.println("LISTA: " + listaCliente.getNome());
                        }
                        Server.jsonobjSend.clear();
                        Server.jsonobjSend.put("COD", "rlogin");
                        Server.jsonobjSend.put("STATUS", "sucesso");
                        cliente.getBuffWr().write(Server.jsonobjSend.toString() + "\r\n");
                        cliente.getBuffWr().flush();
                        System.out.println("SEND: " + Server.jsonobjSend.toString());
                        Server.jsonobjSend.clear();
                        Server.jsonobjSend.put("COD", "chat");
                        Server.jsonobjSend.put("STATUS", "broad");
                        Server.jsonobjSend.put("MSG", "Seja bem-vindo ao CHATeTs " + cliente.getNome() + "!");
                        cliente.getBuffWr().write(Server.jsonobjSend.toString() + "\r\n");
                        cliente.getBuffWr().flush();
                        System.out.println("SEND: " + Server.jsonobjSend.toString());
                        ta.append("** " + cliente.getNome() + "(" + cliente.getIp() + ")" + " connected!\r\n");
                        setScrollMaximum();
                        broadcastMsg(cliente.getBuffWr());
                        Server.jsonobjSend.clear();
                        Server.jsonobjSend.put("COD", "lista");
                        broadcastMsg(cliente.getBuffWr());
                        break;
                    case "logout":
                        //LOGOUT
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        cliente.setIp(this.connection.getInetAddress().getHostAddress());
                        Server.listaClientes.remove(cliente);
                        Server.jsonobjSend.clear();
                        Server.jsonobjSend.put("COD", "chat");
                        Server.jsonobjSend.put("STATUS", "broad");
                        Server.jsonobjSend.put("MSG", cliente.getNome() + " se desconectou do CHATeTs...");
                        broadcastMsg(cliente.getBuffWr());
                        Server.jsonobjSend.clear();
                        Server.jsonobjSend.put("COD", "lista");
                        broadcastMsg(cliente.getBuffWr());
                        Server.jsonobjSend.clear();
                        Server.jsonobjSend.put("COD", "rlogout");
                        Server.jsonobjSend.put("STATUS", "sucesso");
                        cliente.getBuffWr().write(Server.jsonobjSend.toString() + "\r\n");
                        cliente.getBuffWr().flush();
                        System.out.println("SEND: " + Server.jsonobjSend.toString());
                        ta.append("** " + cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!\r\n");
                        setScrollMaximum();
                        break;
                    case "chat":
                        //CHAT
                        msg = (String) jsonReceived.get("STATUS");
                        switch (msg) {
                            case "uni":
                                msg = (String) jsonReceived.get("MSG");
                                cliente.setNome((String) jsonReceived.get("NOME"));
                                Server.jsonobjSend.put("MSG", "(PRIVATE) " + cliente.getNome() + " → " + msg);
                                list = (JSONArray) jsonReceived.get("LISTACLIENTE");
                                Server.jsonobjSend.put("LISTACLIENTES", list);
                                unicastMsg(Server.listaClientes, Server.jsonobjSend);
                                break;
                            case "broad":
                                msg = (String) jsonReceived.get("MSG");
                                cliente.setNome((String) jsonReceived.get("NOME"));
                                Server.jsonobjSend.clear();
                                Server.jsonobjSend.put("COD", "chat");
                                Server.jsonobjSend.put("STATUS", "broad");
                                Server.jsonobjSend.put("MSG", cliente.getNome() + " → " + msg);
                                broadcastMsg(cliente.getBuffWr());
                                Server.jsonobjSend.clear();
                                ta.append(cliente.getNome() + " → " + msg + "\r\n");
                                setScrollMaximum();
                                break;
                        }
                        break;
                }
            }

        } catch (Exception ex) {
            Server.listaClientes.remove(cliente);
            Server.jsonobjSend.clear();
            Server.jsonobjSend.put("COD", "chat");
            Server.jsonobjSend.put("STATUS", "broad");
            Server.jsonobjSend.put("MSG", cliente.getNome() + " se desconectou do CHATeTs...");
            broadcastMsg(cliente.getBuffWr());
            ta.append("** " + cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!\r\n");
            setScrollMaximum();
            Server.jsonobjSend.clear();
            Server.jsonobjSend.put("COD", "lista");
            broadcastMsg(cliente.getBuffWr());
        }
    }

    public void broadcastMsg(BufferedWriter bufWrOUT) {
        BufferedWriter bufWrAUX;
        String msg = (String) Server.jsonobjSend.get("COD");
        JSONArray jsonArr = new JSONArray();
        try {
            if (msg.equals("lista")) {
                for (tipoCliente clt : Server.listaClientes) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("NOME", clt.getNome());
                    jsonObj.put("IP", clt.getIp());
                    jsonArr.add(jsonObj);
                }
                Server.jsonobjSend.put("LISTACLIENTE", jsonArr);
            }
            for (tipoCliente clients : Server.listaClientes) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                if (!(bufWrOUT == bufWrAUX)) {
                    System.out.println("SEND TO " + clients.getNome() + ": " + Server.jsonobjSend.toString());
                    bufWrAUX.write(Server.jsonobjSend.toString() + "\r\n");
                    bufWrAUX.flush();
                }
            }
        } catch (Exception ex) {
            ta.append("Error to send broadcast message...\r\n");
            System.out.println("Except:" + ex);
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

    private static int configPort() {
        JTextField field1 = new JTextField();
        Object[] message = {
            "Server PORT:", field1
        };
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
        ta = new JTextArea(20, 30);
        ta.setEditable(false);
        ta.setLineWrap(true);
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
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
