/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chattcp;

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
import org.json.JSONObject;

/**
 *
 * @author Fernando
 */
public class Server extends Thread {

    private static ArrayList<BufferedWriter> clients = new ArrayList<>();
    private static ArrayList<String> online = new ArrayList<>();
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
    private static JSONObject jsonobj = new JSONObject();

    public Server(Socket sClient) {
        this.connection = sClient;
        ta.append("** " + clientIP + " connected!\r\n");
        setScrollMaximum();
        try {
            in = sClient.getInputStream();
            inRead = new InputStreamReader(in);
            bufRead = new BufferedReader(inRead);
        } catch (IOException ioex) {
            System.err.println("Exception Server Constructor: \n" + ioex);
        }
    }

    @Override
    public void run() {
        try {
            String msg;
            out = this.connection.getOutputStream();
            outWr = new OutputStreamWriter(out);
            buffWr = new BufferedWriter(outWr);
            clients.add(buffWr);
            msg = username = bufRead.readLine();
            online.add(username + " (" + clientIP + ")");
            sendToAllClients(online, buffWr, msg + " connected!\r\n");
            while (msg != null) {
                msg = bufRead.readLine();
                if (msg.equals("EXIT")) {
                    msg = username + " disconnected!\r\n";
                    online.remove(username + " (" + clientIP + ")");
                    sendToAllClients(online, buffWr, msg);
                    ta.append("** " + clientIP + " disconnected!\r\n");
                    setScrollMaximum();
                    System.err.println(clientIP + " disconnected!");
                    clients.remove(buffWr);
                    break;
                } else {
                    sendToAllClients(online, buffWr, msg);
                    ta.append(username + " → " + msg + "\r\n");
                    setScrollMaximum();
                    System.out.println(username + " → " + msg);
                }
            }
        } catch (IOException ioex) {
            ta.append("** " + clientIP + " disconnected!\r\n");
            setScrollMaximum();
            System.err.println(clientIP + " disconnected!");
            online.remove(username + " (" + clientIP + ")");
            sendToAllClients(online, buffWr, username + " disconnected!\r\n");
            clients.remove(buffWr);
        }
    }

    public void sendToAllClients(ArrayList<String> online, BufferedWriter bufWrOUT, String msg) {
        BufferedWriter bufWrAUX;
        try {
            for (BufferedWriter aux : clients) {
                bufWrAUX = (BufferedWriter) aux;
                if (!(bufWrOUT == bufWrAUX)) {
                    if (msg.equals(username + " connected!\r\n")) {
                        aux.write(msg);
                    } else if (msg.equals(username + " disconnected!\r\n")) {
                        aux.write(msg);
                    } else {
                        aux.write(username + " → " + msg + "\r\n");
                    }
                    aux.flush();
                }
            }
        } catch (IOException ioex) {
            System.err.println("Error sendToAllClients()");
        }
    }

    private static void configPort() {
        JTextField field1 = new JTextField();
        Object[] message = {
            "Server PORT:", field1
        };
        int option = JOptionPane.showConfirmDialog(null, message, "SERVER CONFIG", JOptionPane.OK_CANCEL_OPTION);
        if (option == 0) {
            try {
                port = Integer.parseInt(field1.getText());
            } catch (NumberFormatException nbex) {
                JOptionPane.showMessageDialog(null, "Invalid Server PORT!", "ERROR SERVER PORT", JOptionPane.ERROR_MESSAGE);
                configPort();
            }
        } else if (option == -1 || option == 1) {
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
        ta.append("Server opened! " + host + ":" + port + "\n");
        jsonobj.put("hello", "1");
        jsonobj.put("world", "2");
        ta.append(jsonobj.toString() + "\r\n");
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
