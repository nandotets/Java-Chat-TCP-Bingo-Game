/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Misc.CardType;
import Misc.Countdown;
import Misc.ClientType;
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
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author Fernando
 */
public class Server extends Thread {

    public static ArrayList<ClientType> clientList = new ArrayList<>();
    public static ArrayList<ClientType> readyList = new ArrayList<>();
    public static ArrayList<CardType> cardList = new ArrayList<>();
    private static ServerSocket server;
    private Socket connection;
    private InputStream in;
    private InputStreamReader inRead;
    private BufferedReader bufRead;
    private BufferedWriter buffWr;
    private Writer outWr;
    private static Integer drawnb;
    private OutputStream out;
    private static ArrayList<Integer> draw = new ArrayList<>();
    private static JSONObject jsonSend = new JSONObject();
    private static JSONObject jsonReceived = new JSONObject();
    public static Thread countdown = new Countdown(-1);

    public Server(Socket sClient) {
        this.connection = sClient;
        ServerScreen.setScrollMaximum();
        try {
            in = sClient.getInputStream();
            inRead = new InputStreamReader(in);
            bufRead = new BufferedReader(inRead);
        } catch (IOException ioex) {
            //System.err.println("Error: Server Constructor -> " + ioex);
        }
    }

    @Override
    public void run() {
        String msg = "";
        ClientType cliente = new ClientType("", this.connection.getInetAddress().getHostAddress(), String.valueOf(this.connection.getPort()));
        try {
            out = connection.getOutputStream();
            outWr = new OutputStreamWriter(out);
            buffWr = new BufferedWriter(outWr);

            while (msg != null) {

                msg = bufRead.readLine();
                jsonReceived = (JSONObject) JSONValue.parse(msg);
                ServerScreen.areaReceive.append("• " + msg + "\r\n");
                msg = (String) jsonReceived.get("COD");
                switch (msg) {
                    case "bingo": {
                        BufferedWriter bufWrAUX;
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        for (int i = 0; i < cardList.size(); i++) {
                            if (cliente.getNome().equals((String) cardList.get(i).getClient().getNome())) {
                                if (cardList.get(i).getHas().size() >= 3) {;
                                    JSONArray array = new JSONArray();
                                    JSONObject origem = new JSONObject();
                                    origem.put("PORTA", cliente.getPorta());
                                    origem.put("IP", cliente.getIp());
                                    origem.put("NOME", cliente.getNome());
                                    array.add(origem);
                                    jsonSend.clear();
                                    jsonSend.put("CARTELA", null);
                                    jsonSend.put("LISTACLIENTE", array);
                                    jsonSend.put("MSG", null);
                                    jsonSend.put("NOME", null);
                                    jsonSend.put("COD", "rbingo");
                                    jsonSend.put("STATUS", "sucesso");
                                    for (ClientType clients : readyList) {
                                        bufWrAUX = (BufferedWriter) clients.getBuffWr();
                                        bufWrAUX.write(jsonSend.toString() + "\r\n");
                                        bufWrAUX.flush();
                                        ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                                        ServerScreen.setScrollMaximum();
                                    }
                                    gameReset();
                                } else {
                                    JSONArray array = new JSONArray();
                                    JSONObject origem = new JSONObject();
                                    origem.put("PORTA", cliente.getPorta());
                                    origem.put("IP", cliente.getIp());
                                    origem.put("NOME", cliente.getNome());
                                    array.add(origem);
                                    jsonSend.clear();
                                    jsonSend.put("STATUS", "falha");
                                    jsonSend.put("CARTELA", null);
                                    jsonSend.put("LISTACLIENTE", array);
                                    jsonSend.put("MSG", null);
                                    jsonSend.put("NOME", null);
                                    jsonSend.put("COD", "rbingo");
                                    cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                                    ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                                    ServerScreen.setScrollMaximum();
                                    cliente.getBuffWr().flush();

                                }
                            }
                        }
                        break;
                    }

                    case "marca": {
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        JSONArray cartela = (JSONArray) jsonReceived.get("CARTELA");
                        msg = String.valueOf(cartela.get(0));
                        if (drawnb == Integer.valueOf(msg)) {
                            for (int i = 0; i < cardList.size(); i++) {
                                if (cliente.getNome().equals((String) cardList.get(i).getClient().getNome())) {
                                    if (!numCheck(cardList.get(i).getCard(), Integer.valueOf(msg))) {
                                        cardList.get(i).setHas(Integer.valueOf(msg));
                                        ServerScreen.pedras.append("• " + cardList.get(i).getClient().getIp() + "   " + cardList.get(i).getClient().getNome() + "   " + cardList.get(i).getHas() + "\r\n");
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case "pronto": {
                        //Recebe nome
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        msg = (String) jsonReceived.get("STATUS");
                        if (msg.equals("sucesso")) {
                            //Habilita cliente
                            if (readyList.add(cliente)) {
                                System.out.println(cliente.getNome() + "(" + cliente.getIp() + ")" + " ready to play!");
                                ServerScreen.contador.setText("30");
                                Countdown.setNum(30);
                                Countdown.setCount(30);
                                sendCountdown();
                            }
                        } else if (msg.equals("falha")) {
                            //Desabilita cliente
                            if (readyList.remove(cliente)) {
                                System.out.println(cliente.getNome() + "(" + cliente.getIp() + ")" + " unready to play...");
                                if (readyList.isEmpty()) {
                                    Countdown.setNum(-1);
                                    Countdown.setCount(30);
                                    Countdown.setNumGame(-1);
                                    Countdown.setCountGame(10);
                                    ServerScreen.contador.setText("30");
                                    gameReset();
                                }
                            }
                        }
                        jsonSend.clear();
                        jsonSend.put("CARTELA", null);
                        jsonSend.put("STATUS", "sucesso");
                        jsonSend.put("LISTACLIENTE", null);
                        jsonSend.put("MSG", null);
                        jsonSend.put("NOME", null);
                        jsonSend.put("COD", "rpronto");
                        cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                        ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                        ServerScreen.setScrollMaximum();
                        cliente.getBuffWr().flush();
                        sendReadyList();
                        break;
                    }
                    case "login": {
                        //LOGIN
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        clientList.add(cliente);
                        jsonSend.clear();
                        jsonSend.put("CARTELA", null);
                        jsonSend.put("STATUS", "sucesso");
                        jsonSend.put("LISTACLIENTE", null);
                        jsonSend.put("MSG", null);
                        jsonSend.put("NOME", null);
                        jsonSend.put("COD", "rlogin");
                        cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                        ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                        cliente.getBuffWr().flush();
                        System.out.println(cliente.getNome() + "(" + cliente.getIp() + ")" + " connected!");
                        ServerScreen.setScrollMaximum();
                        sendOnlineList();
                        sendReadyList();
                        break;
                    }
                    case "logout": {
                        //LOGOUT
                        cliente.setBuffWr(buffWr);
                        cliente.setNome((String) jsonReceived.get("NOME"));
                        cliente.setIp(this.connection.getInetAddress().getHostAddress());
                        cliente.setPorta(String.valueOf(this.connection.getPort()));
                        if (clientList.remove(cliente)) {
                            readyList.remove(cliente);
                            sendOnlineList();
                            jsonSend.clear();
                            jsonSend.put("CARTELA", null);
                            jsonSend.put("STATUS", "sucesso");
                            jsonSend.put("LISTACLIENTE", null);
                            jsonSend.put("MSG", null);
                            jsonSend.put("NOME", null);
                            jsonSend.put("COD", "rlogout");
                            cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                            ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                            cliente.getBuffWr().flush();
                            System.out.println(cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!");
                        } else {
                            jsonSend.put("CARTELA", null);
                            jsonSend.put("STATUS", "falha");
                            jsonSend.put("LISTACLIENTE", null);
                            jsonSend.put("MSG", null);
                            jsonSend.put("NOME", null);
                            jsonSend.put("COD", "rlogout");
                            cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                            ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                            cliente.getBuffWr().flush();
                        }
                        ServerScreen.setScrollMaximum();
                        break;
                    }
                    case "chat": {
                        //CHAT
                        msg = (String) jsonReceived.get("STATUS");
                        if (msg.equals("uni")) {
                            JSONArray array = (JSONArray) jsonReceived.get("LISTACLIENTE");
                            if (array != null) {
                                JSONObject cDestino = (JSONObject) array.get(0);
                                array.clear();
                                JSONObject origem = new JSONObject();
                                origem.put("PORTA", cliente.getPorta());
                                origem.put("IP", cliente.getIp());
                                origem.put("NOME", cliente.getNome());
                                array.add(origem);

                                msg = (String) jsonReceived.get("MSG");
                                jsonSend.clear();
                                jsonSend.put("CARTELA", null);
                                jsonSend.put("STATUS", "uni");
                                jsonSend.put("LISTACLIENTE", array);
                                jsonSend.put("MSG", msg);
                                jsonSend.put("NOME", null);
                                jsonSend.put("COD", "chat");

                                cliente.getBuffWr().write(jsonSend.toString() + "\r\n");
                                System.out.println(cliente.getNome() + " (PRIVATE TO " + cDestino.get("NOME") + ") → " + msg);
                                ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                                ServerScreen.setScrollMaximum();
                                cliente.getBuffWr().flush();

                                unicastMsg(cDestino);
                            }
                        } else if (msg.equals("broad")) {
                            JSONObject fromCliente = new JSONObject();
                            JSONArray arr = new JSONArray();
                            fromCliente.put("PORTA", cliente.getPorta());
                            fromCliente.put("IP", cliente.getIp());
                            fromCliente.put("NOME", cliente.getNome());
                            arr.add(fromCliente);
                            msg = (String) jsonReceived.get("MSG");
                            jsonSend.clear();
                            jsonSend.put("CARTELA", null);
                            jsonSend.put("STATUS", "broad");
                            jsonSend.put("LISTACLIENTE", arr);
                            jsonSend.put("MSG", msg);
                            jsonSend.put("NOME", null);
                            jsonSend.put("COD", "chat");
                            broadcastMsg();
                            jsonSend.clear();
                            System.out.println(cliente.getNome() + " → " + msg);
                            ServerScreen.setScrollMaximum();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (clientList.remove(cliente)) {
                readyList.remove(cliente);
                System.out.println(cliente.getNome() + "(" + cliente.getIp() + ")" + " disconnected!");
            }
            sendReadyList();
            sendOnlineList();
        }
    }

    public static Boolean startGame() {
        if (readyList.isEmpty()) {
            return false;
        } else {
            for (ClientType readyClient : readyList) {
                ArrayList<Integer> numbers = generateCard(readyClient);
                CardType card = new CardType(readyClient, numbers);
                cardList.add(card);
            }
            sendCards();
            return true;
        }
    }

    public static Integer drawNumber() {
        Random randomD = new Random();
        return randomD.nextInt(75 - 1) + 1;
    }

    public static ArrayList<Integer> generateCard(ClientType client) {
        ArrayList<Integer> B = new ArrayList<>();
        ArrayList<Integer> I = new ArrayList<>();
        ArrayList<Integer> N = new ArrayList<>();
        ArrayList<Integer> G = new ArrayList<>();
        ArrayList<Integer> O = new ArrayList<>();
        Random randomB = new Random();
        Random randomI = new Random();
        Random randomN = new Random();
        Random randomG = new Random();
        Random randomO = new Random();

        for (int b = 1; b <= 15; b++) {
            int randomb = randomB.nextInt(15 - 1) + 1;
            if (numCheck(B, randomb)) {
                B.add(randomb);
            }
        }

        for (int i = 16; i <= 30; i++) {
            int randomi = randomI.nextInt(30 - 16) + 16;
            if (numCheck(I, randomi)) {
                I.add(randomi);
            }
        }

        for (int n = 31; n <= 45; n++) {
            int randomn = randomN.nextInt(45 - 31) + 31;
            if (numCheck(N, randomn)) {
                N.add(randomn);
            }
        }

        for (int g = 46; g <= 60; g++) {
            int randomg = randomG.nextInt(60 - 46) + 46;
            if (numCheck(G, randomg)) {
                G.add(randomg);
            }
        }

        for (int o = 61; o <= 75; o++) {
            int randomo = randomO.nextInt(75 - 61) + 61;
            if (numCheck(O, randomo)) {
                O.add(randomo);
            }
        }

        ArrayList<Integer> card = new ArrayList<>();

        for (int b = 0; b < 5; b++) {
            card.add(B.get(b));
        }
        for (int i = 0; i < 5; i++) {
            card.add(I.get(i));
        }
        for (int n = 0; n < 5; n++) {
            if (n == 2) {
                card.add(0);
            } else {
                card.add(N.get(n));
            }
        }
        for (int g = 0; g < 5; g++) {
            card.add(G.get(g));
        }
        for (int o = 0; o < 5; o++) {
            card.add(O.get(o));
        }

        return card;
    }

    public static boolean numCheck(ArrayList<Integer> box, Integer n) {
        if (box.contains(n)) {
            return false;
        }
        return true;
    }

    public void broadcastMsg() {
        BufferedWriter bufWrAUX;
        try {
            for (ClientType clients : clientList) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                ServerScreen.setScrollMaximum();
                bufWrAUX.write(jsonSend.toString() + "\r\n");
                bufWrAUX.flush();
            }
        } catch (IOException ex) {
            //System.err.println("Error to send broadcast message: " + ex);
        }
        jsonSend.clear();
    }

    public void unicastMsg(JSONObject destino) {
        try {
            for (ClientType clients : clientList) {
                String nome = clients.getNome();
                String ip = clients.getIp();
                if (nome.equals(destino.get("NOME").toString()) && ip.equals(destino.get("IP").toString())) {
                    ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                    ServerScreen.setScrollMaximum();
                    clients.getBuffWr().write(jsonSend.toString() + "\r\n");
                    clients.getBuffWr().flush();
                    break;
                }
            }
            jsonSend.clear();
        } catch (Exception ex) {
            //System.err.println("Error to send unicast message: " + ex);
        }
    }

    public void sendOnlineList() {
        BufferedWriter bufWrAUX;
        int i = 1;
        JSONArray jsonArr = new JSONArray();
        jsonSend.clear();
        jsonSend.put("COD", "lista");
        try {
            ServerScreen.areaOnline.setText("");
            for (ClientType clt : clientList) {
                ServerScreen.areaOnline.append(" " + i + ") " + clt.getNome() + " (" + clt.getIp() + ":" + clt.getPorta() + ")\r\n");
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("PORTA", clt.getPorta());
                jsonObj.put("IP", clt.getIp());
                jsonObj.put("NOME", clt.getNome());
                jsonArr.add(jsonObj);
                i++;
            }
            jsonSend.put("LISTACLIENTE", jsonArr);

            for (ClientType clients : clientList) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                ServerScreen.setScrollMaximum();
                bufWrAUX.write(jsonSend.toString() + "\r\n");
                bufWrAUX.flush();
            }
            jsonArr.clear();
            jsonSend.clear();
        } catch (Exception ex) {
            //System.err.println("Error to send OnlineListBroadcast: " + ex);
        }
    }

    public void sendReadyList() {
        BufferedWriter bufWrAUX;
        int i = 1;
        JSONArray jsonArr = new JSONArray();
        ServerScreen.areaReady.setText("");
        for (ClientType clienteHabilitado : readyList) {
            ServerScreen.areaReady.append(" " + i + ") " + clienteHabilitado.getNome() + " (" + clienteHabilitado.getIp() + ":" + clienteHabilitado.getPorta() + ")\r\n");
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("PORTA", clienteHabilitado.getPorta());
            jsonObj.put("IP", clienteHabilitado.getIp());
            jsonObj.put("NOME", clienteHabilitado.getNome());
            jsonArr.add(jsonObj);
            i++;
        }

        if (jsonArr.isEmpty()) {
            jsonArr = null;
        }

        try {
            jsonSend.clear();
            jsonSend.put("CARTELA", null);
            jsonSend.put("STATUS", null);
            jsonSend.put("LISTACLIENTE", jsonArr);
            jsonSend.put("MSG", null);
            jsonSend.put("NOME", null);
            jsonSend.put("COD", "listapronto");

            for (ClientType clients : readyList) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                bufWrAUX.write(jsonSend.toString() + "\r\n");
                bufWrAUX.flush();
                ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                ServerScreen.setScrollMaximum();
            }
        } catch (Exception ex) {
            //System.err.println("Error to send broadcast message: " + ex);
            ServerScreen.setScrollMaximum();
        }
        jsonSend.clear();
    }

    public void sendCountdown() {
        BufferedWriter bufWrAUX;
        try {
            jsonSend.clear();
            jsonSend.put("CARTELA", null);
            jsonSend.put("STATUS", null);
            jsonSend.put("LISTACLIENTE", null);
            jsonSend.put("MSG", null);
            jsonSend.put("NOME", null);
            jsonSend.put("COD", "tempo");

            for (ClientType clients : clientList) {
                bufWrAUX = (BufferedWriter) clients.getBuffWr();
                bufWrAUX.write(jsonSend.toString() + "\r\n");
                bufWrAUX.flush();
                ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                ServerScreen.setScrollMaximum();
            }
        } catch (Exception ex) {

            ServerScreen.setScrollMaximum();
        }
    }

    public static void sendCards() {
        BufferedWriter bufWrAUX;
        if (!readyList.isEmpty()) {
            try {
                jsonSend.clear();
                jsonSend.put("CARTELA", cardList);
                jsonSend.put("STATUS", null);
                jsonSend.put("LISTACLIENTE", null);
                jsonSend.put("MSG", null);
                jsonSend.put("NOME", null);
                jsonSend.put("COD", "cartela");

                for (CardType card : cardList) {
                    jsonSend.put("CARTELA", card.getCard());
                    bufWrAUX = (BufferedWriter) card.getClient().getBuffWr();
                    bufWrAUX.write(jsonSend.toString() + "\r\n");
                    bufWrAUX.flush();
                    ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                    ServerScreen.setScrollMaximum();
                }
            } catch (Exception ex) {
                if (readyList.isEmpty()) {
                    Countdown.setNum(30);
                    Countdown.setCount(-1);
                    Countdown.setNumGame(10);
                    Countdown.setCountGame(-1);
                    ServerScreen.contador.setText("30");
                }
                ServerScreen.setScrollMaximum();
            }
        }
    }

    public static void sendNumber() {
        if (!readyList.isEmpty()) {
            BufferedWriter bufWrAUX;
            drawnb = drawNumber();
            try {
                jsonSend.clear();
                while (!numCheck(draw, drawnb)) {
                    drawnb = drawNumber();
                }
                draw.add(drawnb);
                ServerScreen.pedras.append(draw.toString()+"\r\n");
                ArrayList<Integer> drawarray = new ArrayList<>();
                drawarray.add(drawnb);
                jsonSend.put("CARTELA", drawarray);
                jsonSend.put("STATUS", null);
                jsonSend.put("LISTACLIENTE", null);
                jsonSend.put("MSG", null);
                jsonSend.put("NOME", null);
                jsonSend.put("COD", "sorteado");
                for (CardType card : cardList) {
                    bufWrAUX = (BufferedWriter) card.getClient().getBuffWr();
                    bufWrAUX.write(jsonSend.toString() + "\r\n");
                    bufWrAUX.flush();
                    ServerScreen.areaSend.append("• " + jsonSend.toString() + "\r\n");
                    ServerScreen.setScrollMaximum();
                }
            } catch (Exception ex) {
                if (readyList.isEmpty()) {
                    Countdown.setNum(30);
                    Countdown.setCount(-1);
                    Countdown.setNumGame(10);
                    Countdown.setCountGame(-1);
                    ServerScreen.contador.setText("30");
                }
                ServerScreen.setScrollMaximum();
            }
        } else {
            Countdown.setNum(30);
            Countdown.setCount(-1);
            Countdown.setNumGame(10);
            Countdown.setCountGame(-1);
            ServerScreen.contador.setText("30");
            ServerScreen.setScrollMaximum();
        }
    }

    private static void gameReset() {
        cardList.clear();
        draw.clear();
        ServerScreen.pedras.setText("");
        
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
        ServerScreen frame = new ServerScreen();
        frame.setTitle("CHATeTs Server. PORT:" + port);
        frame.setLocationRelativeTo(null);
        System.out.println("Server opened! " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            int portConfig = configPort();
            server = new ServerSocket(portConfig);
            initComponents(portConfig);
            countdown.start();
            while (true) { //Mantem servidor ativo mesmo com desconexões de clientes
                Socket clientC = server.accept();
                Thread t = new Server(clientC);
                t.start();
            }
        } catch (IOException ioex) {
            JOptionPane.showMessageDialog(null, "Another server already exists on this pc!", "ERRO", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException argex) {
            //System.err.println("Invalid PORT!");
        }
    }
}
