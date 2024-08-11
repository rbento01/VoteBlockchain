/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package blockchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.Serializable;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author bento
 */
public class Blockchain implements Serializable {

    //lista de blocos
    List<Block> chain;

    //cria uma nova lista 
    public Blockchain() {
        chain = new CopyOnWriteArrayList<>();
        //chain.add(new Block("Previous", "dados"));
    }

    public String getChain() {
        return chain.toString();
    }

    public String toString() {
        StringBuilder txt = new StringBuilder();
        for (Block bloco : chain) {
            txt.append(bloco.toString()).append("\n");
        }
        return txt.toString();
    }

    //cria um novo bloco com o hash do anterior
    public Block createBlock(String data) {
        String ultimo = getLastBlock();
        Block bloco = new Block(ultimo, data);
        return bloco;
    }

    //retorna o último bloco
    public String getLastBlock() {
        String bloco = "";
        if (chain.isEmpty()) {
            bloco = "000";
        } else {
            bloco = chain.get(chain.size() - 1).getHash();
        }

        return bloco;
    }

    //adiciona um novo bloco
    public void add(Block bloco) {
        if (!chain.contains(bloco)) {
            chain.add(bloco);
        }
    }

    public void save(String file) throws FileNotFoundException {
        PrintStream out = new PrintStream(new File(file));
        out.println(toString());
        out.close();
    }

    public static Blockchain load(String filName) throws FileNotFoundException {
        Scanner file = new Scanner(new File(filName));
        Blockchain blkTmp = new Blockchain();
        blkTmp.chain.clear();
        while (file.hasNext()) {
            //ler uma linha
            String line = file.nextLine();
            //partir a linha nos elementos
            String[] elem = line.split(" ");
            Block b = new Block(elem[0], elem[3] + " " + elem[4] + " " + elem[5]);
            b.hash = elem[2];
            b.nonce = parseInt(elem[1]);
            //adicionar o bloco ao objeto
            blkTmp.chain.add(b);
        }
        return blkTmp;
    }

    /**
     *
     * @return
     */
    public ArrayList getName() {
        ArrayList<String> arr = new ArrayList<>();
        for (Block bloco : chain) {
            arr.add(bloco.getData());
        }
        return arr;
    }

    public int getSize() {
        int contador = 0;
        for (Block bloco : chain) {
            contador++;
        }
        return contador;
    }

    public boolean contains(Block b) {
        for (Block bloco : chain) {
            if (bloco.getHash().equals(b.getHash())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getData() throws FileNotFoundException {
//        ArrayList<String> data = new ArrayList<String>();
//        String filName = "VoteChain.txt";
//        Scanner file = new Scanner(new File(filName));
//        if (!chain.isEmpty()) {
//            while (file.hasNext()) {
//                //ler uma linha
//                String line = file.nextLine();
//                //partir a linha nos elementos
//                String[] elem = line.split(" ");
//                data.add(elem[3]);
//            }
//        }
        ArrayList<String> data = new ArrayList<String>();
        for (Block bloco : chain) {
            data.add(bloco.getData().substring(0, 8));
        }
        
    
        return data;
    }

}
