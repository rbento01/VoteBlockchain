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

import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created on 02/11/2021, 18:28:06
 *
 * @author IPT - computer
 */
public class MinerThread extends Thread {

    //atributos da thread
    MiningListener listener; // notificação da mineração
    private final AtomicInteger sharedNonce;  // referência para o global nonce
    private final AtomicInteger sharedTicket; // referência para distribuidor de tickets
    private final String message;             // referência para a mensagem do bloco
    private final int zeros;                  // número de zeros

    public MinerThread(AtomicInteger nonce, AtomicInteger ticket, String message, int zeros, MiningListener listener) {
        this.sharedNonce = nonce;
        this.sharedTicket = ticket;
        this.message = message;
        this.zeros = zeros;
        this.listener = listener;
    }

    @Override
    public void run() {
        String prefix = String.format("%0" + zeros + "d", 0);
        //enquanto não for encontrado o nonce ( nonce <= 0 )
        while (sharedNonce.get() <= 0) {
            try {
                //tirar um ticket e testá-lo
                int number = sharedTicket.getAndIncrement();
                //verificar se o hash esta correto
                if (getHash(message, number).startsWith(prefix)) {
                    //atualizar o nonce e terminar as threads
                    sharedNonce.set(number);
                    //notify listeners
                    if (listener != null) {
                        listener.onNounceFound(number, message);
                        listener.updateVoters();
                    }
                }
                //notify listeners every 100 numbers
                if (listener != null && number % 100 == 0) {
                    listener.onMining(number);
                }
            } catch (Exception ex) {
                Logger.getLogger(MinerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (listener != null) {
            listener.onStopMining(Thread.currentThread().getName(), sharedNonce.get());
        }

    }

    public boolean isValidNonce() {
        try {
            return getHash().startsWith(String.format("%0" + zeros + "d", 0));
        } catch (Exception ex) {
            return false;
        }
    }

    public String getHash() throws Exception {
        return getHash(message + sharedNonce.get(), "SHA-256");
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::      I N T E G R I T Y         :::::::::::::::::::::::::::::::::    
    ///////////////////////////////////////////////////////////////////////////
    public static String getHash(String data, int nonce) throws Exception {
        return getHash(data + nonce, "SHA-256");
    }

    public static String getHash(String data, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(data.getBytes());
        return Base64.getEncoder().encodeToString(md.digest());
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202111021828L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2021  :::::::::::::::::::
    ///////////////////////////////////////////////////////////////////////////
}
