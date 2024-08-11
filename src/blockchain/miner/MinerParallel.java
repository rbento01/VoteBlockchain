//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::     Biosystems & Integrative Sciences Institute                         ::
//::     Faculty of Sciences University of Lisboa                            ::
//::     http://www.fc.ul.pt/en/unidade/bioisi                               ::
//::                                                                         ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2021   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
//////////////////////////////////////////////////////////////////////////////
package blockChain.miner;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 02/11/2021, 18:28:06
 *
 * @author IPT - computer
 */
public class MinerParallel {

    //Balanceamento da mineração          
    private AtomicInteger sharedNonce; // Nonce da mineração (partilhado)
    private AtomicInteger sharedTicket;// Ticket a ser testado (partilhado)
    private String message;            // Mensagem a ser minada (Partilhado)
    
    private MinerThread[] threads;     // Array de threads
    private ExecutorService exe;       // executor das threads
    
    MiningListener listener;           // para actualizar as GUI



    public MinerParallel(MiningListener listener) {

        this.listener = listener;
    }

    /**
     * inicia a mineração de uma mensagem
     *
     * @param message mensagem
     * @param zeros número de zeros do hash
     * @throws Exception
     */
    public void startMining(String message, int zeros) throws Exception {
        //is working
        if (isMining()) {
            stopMining(99999);
        }
        this.message = message;
        //initialize random ticket
        Random rnd = new Random();
        //with a positive integer number
        sharedTicket = new AtomicInteger(Math.abs(rnd.nextInt() / 2));
        //configurar os atributos    
        int numCores =  Runtime.getRuntime().availableProcessors();
        threads = new MinerThread[numCores];
        exe = Executors.newFixedThreadPool(numCores);
        sharedNonce = new AtomicInteger();

        //executar as threads
        for (int i = 0; i < numCores; i++) {
            threads[i] = new MinerThread(sharedNonce, sharedTicket, message, zeros, listener);
            exe.execute(threads[i]);
        }
        //fechar a pool
        exe.shutdown();
        //notify listener
        if (listener != null) {
            listener.onStartMining(message, zeros);
        }
    }

    public String getHash() {
        try {
            return MinerThread.getHash(message, sharedNonce.get());
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    /**
     * terminar a mineração
     *
     * @param nonce numero maior que zero
     */
    public void stopMining(int nonce) {
        sharedNonce.set(nonce);       
    }

    /**
     * verificar se está a minerar
     *
     * @return está a minerar
     */
    public boolean isMining() {
        return sharedNonce != null && sharedNonce.get() <= 0;
    }

    /**
     * devolve o resultado da mineração ou zero
     *
     * @return nonce
     */
    public int getNonce() {
        return sharedNonce.get();
    }

    /**
     * devolve o ticket que está a ser testado
     *
     * @return nonce
     */
    public int getTicket() {
        return sharedTicket.get();
    }

    /**
     * devolve o resultado da mineração ou zero
     *
     * @return nonce
     * @throws java.lang.InterruptedException
     */
    public int waitToNonce() throws InterruptedException {
        exe.awaitTermination(1, TimeUnit.DAYS);
        return sharedNonce.get();
    }

    /**
     * calcula o valor do nonce da mensagem
     *
     * @param message mensagem
     * @param zeros número de zeros
     * @return
     * @throws Exception
     */
    public int mine(String message, int zeros) throws Exception {
        startMining(message, zeros);
        exe.awaitTermination(1, TimeUnit.DAYS);
        return sharedNonce.get();
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202111021828L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2021  :::::::::::::::::::
    ///////////////////////////////////////////////////////////////////////////
}
