/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Tipos.tipoCliente;
import java.awt.Dimension;
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
    public static ArrayList<tipoCliente> listaHabilitados = new ArrayList<>();
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
                jsonReceived = (JSONObject) JSONValue.parse(msg);
                ta.append("RECEIVE: " + msg + "\r\n");
                msg = (String) jsonReceived.get("COD");
                switch (msg) {
                    case "pronto": {
                        //Recebe nome
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        msg = (String) jsonReceived.get("STATUS");
                        if (msg.equals("sucesso")) {
                            //Habilita cliente
                            listaHabilitados.add(cliente);
                            ta.append("--->" + cliente.getNome() + "(" + cliente.getIp() + ")" + " habilitado!\r\n");
                        } else {
                            //Desabilita cliente
                            listaHabilitados.remove(cliente);
                            ta.append("--->" + cliente.getNome() + "(" + cliente.getIp() + ")" + " desabilitado!\r\n");
                        }
                        sendReadyList();
                        break;
                    }
                    case "login": {
                        //LOGIN
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        listaClientes.add(cliente);

                        jsonSend.clear();
                        jsonSend.put("STATUS", "sucesso");
                        jsonSend.put("LISTACLIENTE", null);
                        jsonSend.put("MSG", null);
                        jsonSend.put("NOME", null);
                        jsonSend.put("COD", "rlogin");
                        cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                        cliente.getBuffWr().flush();
                        ta.append("SEND TO '" + cliente.getNome() + "': " + jsonSend.toString() + "\r\n");
                        ta.append("--->" + cliente.getNome() + "(" + cliente.getIp() + ")" + " connected!\r\n");
                        jsonSend.clear();
                        jsonSend.put("STATUS", "broad");
                        jsonSend.put("LISTACLIENTE", null);
                        jsonSend.put("MSG", "Seja bem-vindo ao CHATeTs " + cliente.getNome() + "!");
                        jsonSend.put("NOME", null);
                        jsonSend.put("COD", "chat");
                        cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                        cliente.getBuffWr().flush();
                        ta.append("SEND TO '" + cliente.getNome() + "': " + jsonSend.toString() + "\r\n");
                        setScrollMaximum();
                        broadcastMsg(cliente.getBuffWr());
                        sendOnlineListBroadcast();
                        break;
                    }
                    case "logout": {
                        //LOGOUT
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        cliente.setIp(this.connection.getInetAddress().getHostAddress());
                        cliente.setPorta(String.valueOf(this.connection.getPort()));
                        if (listaClientes.remove(cliente)) {
                            listaHabilitados.remove(cliente);
                            sendOnlineListBroadcast();
                            jsonSend.clear();
                            jsonSend.put("STATUS", "broad");
                            jsonSend.put("LISTACLIENTE", null);
                            jsonSend.put("MSG", cliente.getNome() + " se desconectou do CHATeTs...");
                            jsonSend.put("NOME", null);
                            jsonSend.put("COD", "chat");
                            broadcastMsg(cliente.getBuffWr());
                            jsonSend.clear();
                            jsonSend.put("STATUS", "sucesso");
                            jsonSend.put("LISTACLIENTE", null);
                            jsonSend.put("MSG", null);
                            jsonSend.put("NOME", null);
                            jsonSend.put("COD", "rlogout");
                            ta.append("SEND TO '" + cliente.getNome() + "': " + jsonSend.toString() + "\r\n");
                            cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                            cliente.getBuffWr().flush();
                            ta.append("--> " + cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!\r\n");
                        } else {
                            jsonSend.put("STATUS", "falha");
                            jsonSend.put("LISTACLIENTE", null);
                            jsonSend.put("MSG", null);
                            jsonSend.put("NOME", null);
                            jsonSend.put("COD", "rlogout");
                            cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                            cliente.getBuffWr().flush();
                            ta.append("SEND TO '" + cliente.getNome() + "': " + jsonSend.toString() + "\r\n");
                        }
                        setScrollMaximum();
                        break;
                    }
                    case "chat": {
                        //CHAT
                        msg = (String) jsonReceived.get("STATUS");
                        if (msg.equals("uni")) {
                            jsonSend.clear();
                            jsonSend.put("STATUS", "uni");
                            JSONObject fromCliente = new JSONObject();
                            JSONArray arr = new JSONArray();
                            fromCliente.put("PORTA", cliente.getPorta());
                            fromCliente.put("IP", cliente.getIp());
                            fromCliente.put("NOME", cliente.getNome());
                            arr.add(fromCliente);
                            jsonSend.put("LISTACLIENTE", arr);
                            msg = (String) jsonReceived.get("MSG");
                            jsonSend.put("MSG", msg);
                            msg = (String) jsonReceived.get("NOME");
                            jsonSend.put("NOME", msg);
                            jsonSend.put("COD", "chat");
                            JSONArray array = (JSONArray) jsonReceived.get("LISTACLIENTE");
                            JSONObject cDestino = (JSONObject) array.get(0);
                            unicastMsg(cDestino);
                        } else if (msg.equals("broad")) {
                            jsonSend.clear();
                            jsonSend.put("STATUS", "broad");
                            jsonSend.put("LISTACLIENTE", null);
                            msg = (String) jsonReceived.get("MSG");
                            jsonSend.put("MSG", jsonReceived.get("NOME") + " → " + msg);
                            jsonSend.put("NOME", null);
                            jsonSend.put("COD", "chat");
                            broadcastMsg(cliente.getBuffWr());
                            jsonSend.clear();
                            System.out.println(jsonReceived.get("NOME") + " → " + msg);
                            setScrollMaximum();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (listaClientes.remove(cliente)) {
                listaHabilitados.remove(cliente);
                jsonSend.clear();
                jsonSend.put("STATUS", "broad");
                jsonSend.put("LISTACLIENTE", null);
                jsonSend.put("MSG", cliente.getNome() + " se desconectou do CHATeTs...");
                jsonSend.put("NOME", null);
                jsonSend.put("COD", "chat");
                broadcastMsg(cliente.getBuffWr());
                ta.append("--> " + cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!\r\n");
            }
            sendOnlineListBroadcast();
            setScrollMaximum();
        }
    }

    public void broadcastMsg(BufferedWriter bufWrOUT) {
        BufferedWriter bufWrAUX;
        String msg = (String) jsonSend.get("COD");
        try {
            for (tipoCliente clients : listaClientes) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                if (!(bufWrOUT == bufWrAUX)) {
                    ta.append("SEND TO '" + clients.getNome() + "': " + jsonSend.toString() + "\r\n");
                    setScrollMaximum();
                    bufWrAUX.write(jsonSend.toString() + "\r\n");
                    bufWrAUX.flush();
                }
            }
        } catch (Exception ex) {
            ta.append("Error to send broadcast message...\r\n");
            System.err.println("Error to send broadcast message: " + ex);
            setScrollMaximum();
        }
        jsonSend.clear();
    }

    public void sendOnlineListBroadcast() {
        BufferedWriter bufWrAUX;
        JSONArray jsonArr = new JSONArray();
        jsonSend.clear();
        jsonSend.put("COD", "lista");
        try {
            for (tipoCliente clt : listaClientes) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("PORTA", clt.getPorta());
                jsonObj.put("IP", clt.getIp());
                jsonObj.put("NOME", clt.getNome());
                jsonArr.add(jsonObj);
            }
            jsonSend.put("LISTACLIENTE", jsonArr);

            for (tipoCliente clients : listaClientes) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                ta.append("SEND TO '" + clients.getNome() + "': " + jsonSend.toString() + "\r\n");
                setScrollMaximum();
                bufWrAUX.write(jsonSend.toString() + "\r\n");
                bufWrAUX.flush();
            }
            jsonArr.clear();
            jsonSend.clear();
        } catch (Exception ex) {
            ta.append("Error to send online list broadcast ...\r\n");
            System.err.println("Error to send OnlineListBroadcast: " + ex);
            setScrollMaximum();
        }
    }

    public void unicastMsg(JSONObject destino) {
        try {
            for (tipoCliente clients : listaClientes) {
                String nome = clients.getNome();
                String ip = clients.getIp();
                if (nome.equals(destino.get("NOME").toString()) && ip.equals(destino.get("IP").toString())) {
                    ta.append("SEND TO '" + nome + "': " + jsonSend.toString() + "\r\n");
                    setScrollMaximum();
                    clients.getBuffWr().write(jsonSend.toString() + "\r\n");
                    clients.getBuffWr().flush();
                    break;
                }
            }
            jsonSend.clear();
        } catch (Exception ex) {
            ta.append("Error to send unicast message...\r\n");
            System.err.println("Error to send unicast message: " + ex);
            setScrollMaximum();
        }
    }

    public void sendReadyList() {
        BufferedWriter bufWrAUX;
        JSONArray jsonArr = new JSONArray();
        for (tipoCliente clienteHabilitado : listaHabilitados) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("PORTA", clienteHabilitado.getPorta());
            jsonObj.put("IP", clienteHabilitado.getIp());
            jsonObj.put("NOME", clienteHabilitado.getNome());
            jsonArr.add(jsonObj);
        }
        try {
            jsonSend.clear();
            jsonSend.put("STATUS", "sucesso");
            jsonSend.put("LISTACLIENTE", jsonArr);
            jsonSend.put("MSG", null);
            jsonSend.put("NOME", null);
            jsonSend.put("COD", "rpronto");
            
            for (tipoCliente clients : listaClientes) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                bufWrAUX.write(jsonSend.toString() + "\r\n");
                bufWrAUX.flush();
                ta.append("SEND TO '" + clients.getNome() + "': " + jsonSend.toString() + "\r\n");
                setScrollMaximum();
            }
        } catch (Exception ex) {
            ta.append("Error to send broadcast message...\r\n");
            System.err.println("Error to send broadcast message: " + ex);
            setScrollMaximum();
        }
        jsonSend.clear();
    }

    private static int configPort() {
        JTextField field1 = new JTextField();

        Object[] message = {
            "Server PORT:", field1
        };

        field1.setText("25000");

        int option = JOptionPane.showConfirmDialog(null, message, "SERVER CONFIG", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                return Integer.parseInt(field1.getText());
            } catch (NumberFormatException nbex) {
                JOptionPane.showMessageDialog(null, "Invalid Server PORT!", "ERROR SERVER PORT", JOptionPane.ERROR_MESSAGE);
                configPort();
            }
        } else if (option == JOptionPane.CLOSED_OPTION || option == JOptionPane.CANCEL_OPTION) {
            int confirm = JOptionPane.showConfirmDialog(null, "Cancel to open server?", "EXIT", JOptionPane.YES_NO_OPTION);
            if (confirm == 0) {
                System.exit(1);
            }
        }
        return 0;
    }

    private static void initComponents(int port) throws UnknownHostException {
        frame = new JFrame("CHATeTs Server. PORT:" + port);
        ta = new JTextArea(35, 85);
        ta.setEditable(false);
        ta.setLineWrap(true);
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(980, 650);
        frame.setMinimumSize(new Dimension(980, 650));
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
