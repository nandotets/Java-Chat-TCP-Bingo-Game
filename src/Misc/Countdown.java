/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Misc;

import Client.ReadyBingoScreen;
import Server.Server;
import Server.ServerScreen;

/**
 *
 * @author Fernando
 */
public class Countdown extends Thread implements Runnable {

    private static int num;
    private static int count;

    public Countdown(int num) {
        //o num vai receber o numero que vai ser descrescido até 0;
        Countdown.num = num;
    }

    @Override
    public void run() {
        setCount(getNum());
        while (true) {
            System.out.flush();
            if (getNum() != -1) {
                try {
                    ServerScreen.contador.setText(getCount() + "");
                    Thread.sleep(1000);
                    setCount(getCount() - 1);
                    if (getCount() == -1) {
                        if (!Server.startGame()) {
                            Countdown.setCount(30);
                            Countdown.setNum(30);
                        } else {
                            Countdown.setCount(30);
                            Countdown.setNum(-1);
                        }
                    }
                } catch (Exception ex) {
                    countdownClient();
                }
            }
        }
    }

    private void countdownClient() {
        while (true) {
            if (getNum() != -1) {
                try {
                    ReadyBingoScreen.contador.setText(getCount() + "");
                    Thread.sleep(1000);
                    setCount(getCount() - 1);
                    if (getCount() == -1) {
                        setCount(getNum());
                    }
                } catch (Exception ex) {
                }
            }
        }

    }

    /**
     * @return the num
     */
    public static int getNum() {
        return num;
    }

    /**
     * @param aNum the num to set
     */
    public static void setNum(int aNum) {
        num = aNum;
    }

    /**
     * @return the count
     */
    public static int getCount() {
        return count;
    }

    /**
     * @param aCount the count to set
     */
    public static void setCount(int aCount) {
        count = aCount;
    }
}
