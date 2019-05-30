/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tipos;

import java.io.BufferedWriter;

/**
 *
 * @author Fernando
 */
public class tipoCliente {

    private String nome, ip, porta;
    private BufferedWriter buffWr;

    public tipoCliente(String nome, String ip, String porta) {
        this.ip = ip;
        this.nome = nome;
        this.porta = porta;
    }

    /**
     * @return the nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param nome the nome to set
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the buffWr
     */
    public BufferedWriter getBuffWr() {
        return buffWr;
    }

    /**
     * @param buffWr the buffWr to set
     */
    public void setBuffWr(BufferedWriter buffWr) {
        this.buffWr = buffWr;
    }

    /**
     * @return the porta
     */
    public String getPorta() {
        return porta;
    }

    /**
     * @param porta the porta to set
     */
    public void setPorta(String porta) {
        this.porta = porta;
    }

}
