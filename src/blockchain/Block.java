/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchain;

import java.io.Serializable;

/**
 *
 * @author bento
 */
public class Block implements Serializable {

    String previous;
    String data;
    int nonce; //garantir a seguranca
    String hash; // hash( previous + data + nonce)
    String key;

    public Block(String previous, String data) {
        this.previous = previous;
        this.data = data;
    }

    public Block(int nonce, String data, String previous) {
        this.nonce = nonce;
        this.data = data;
        this.previous = previous;
    }
    
    @Override
    public String toString() {
        return "[" + previous + "] " + nonce + " [" + hash + "] " + data;

    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean equals(Object obj) {
        return ((Block) obj).hash.equals(this.hash);
    }
}
