/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Misc;

import java.util.ArrayList;

/**
 *
 * @author Fernando
 */
public class CardType {

    private ClientType client;
    private ArrayList<Integer> card;

    public CardType(ClientType client, ArrayList<Integer> card) {
        this.client = client;
        this.card = card;
    }

    /**
     * @return the client
     */
    public ClientType getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(ClientType client) {
        this.client = client;
    }

    /**
     * @return the card
     */
    public ArrayList<Integer> getCard() {
        return card;
    }

    /**
     * @param card the card to set
     */
    public void setCard(ArrayList<Integer> card) {
        this.card = card;
    }

}
