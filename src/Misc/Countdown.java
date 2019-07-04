/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Misc;

import Client.BingoScreen;
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

    private static int numGame;
    private static int countGame;

    public Countdown(int num) {
        //o num vai receber o numero que vai ser descrescido até 0;
        Countdown.num = num;
        Countdown.numGame = num;
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
                    if (getCount() == 0) {
                        if (Server.startGame()) {
                            setCountGame(10);
                            setNumGame(10);
                            setCount(30);
                            setNum(-1);
                            chose();
                        } else {
                            setCount(30);
                            setNum(30);
                            setCountGame(-1);
                            setNumGame(10);
                        }
                    }
                } catch (Exception ex) {
                    countdownClient();
                }
            }
        }
    }

    public void chose() {
        while (true) {
            System.out.flush();
            if (getNumGame() != -1) {
                try {
                    ServerScreen.contador.setText(getCountGame() + "");
                    Thread.sleep(1000);
                    setCountGame(getCountGame() - 1);
                    if (getCountGame() == -1) {
                        Server.sendNumber();
                        Countdown.setCountGame(10);
                        Countdown.setNumGame(10);
                    }
                } catch (Exception ex) {
                    countdownGameClient();
                }
            } else {
                break;
            }
        }
    }

    private void countdownClient() {
        while (true) {
            System.out.flush();
            if (getNum() != -1) {
                try {
                    ReadyBingoScreen.contador.setText(getCount() + "");
                    Thread.sleep(1000);
                    setCount(getCount() - 1);
                    if (getCount() == -1) {
                        setCountGame(10);
                        setNumGame(10);
                        setCount(getNum());
                        countdownGameClient();
                    }
                } catch (Exception ex) {
                }
            }
        }

    }

    private void countdownGameClient() {
        while (true) {
            System.out.flush();
            if (getNumGame() != -1) {
                try {
                    BingoScreen.contador.setText(getCountGame() + "");
                    Thread.sleep(1000);
                    setCountGame(getCountGame() - 1);
                    if (getCountGame() == -1) {
                        setCountGame(getNumGame());
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
     * @return the numGame
     */
    public static int getNumGame() {
        return numGame;
    }

    /**
     * @param aNum the numGame to set
     */
    public static void setNumGame(int aNum) {
        numGame = aNum;
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

    /**
     * @return the countGame
     */
    public static int getCountGame() {
        return countGame;
    }

    /**
     * @param aCount the countGame to set
     */
    public static void setCountGame(int aCount) {
        countGame = aCount;
    }
}
