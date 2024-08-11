package blockChain.miner.remote;

import blockChain.miner.MinerParallel;
import blockchain.Block;
import blockchain.Blockchain;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import myUtils.RMI;

/**
 *
 * @author aluno
 */
public class RemoteMiner extends UnicastRemoteObject implements IminerRemote {

    // endereço do objeto remoto
    String address;
    // listener para atualizar a GUI
    RemoteMinierListener listener;
    // objeto para minar
    MinerParallel miner;
    // Lista de nos com a rede mineira
    List<IminerRemote> network;
    // blockchain dos votos
    public Blockchain chainVote;

    String data;

    /**
     * constructor
     *
     * @param port number of the port
     * @param listener
     * @throws RemoteException
     */
    public RemoteMiner(int port, RemoteMinierListener listener) throws RemoteException, FileNotFoundException, Exception {
        //passar a porta para superclasse (UnicastRemoteObject)
        super(port);
        try {
            //atualizar o listener
            this.listener = listener;
            //criar um mineiro
            this.miner = new MinerParallel(listener);
            //criar a lista da rede (CopyOnWriteArrayList permite o acesso concorrente, outras estruturas de dados não permitem)
            this.network = new CopyOnWriteArrayList<>();
            //atualizar o endereço do objeto remoto
            this.address = RMI.getRemoteName(InetAddress.getLocalHost().getHostAddress(), port, IminerRemote.NAME);
            //vai ter a blockchain vinda do ficheiro
            try {
                this.chainVote = Blockchain.load("VoteChain.txt");
//            System.out.println("Votechain: " + votechain.toString());
            } catch (FileNotFoundException ex) {
                this.chainVote = new Blockchain();

            }
            //:::::::::: Notificar o listener ::::::::::::::
            if (listener != null) {
                listener.onStart(this);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(RemoteMiner.class.getName()).log(Level.SEVERE, null, ex);
            throw new RemoteException("Construtor", ex);
        }
    }

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::                                                         :::::::::::::
    //:::::                R E D E   M I N E I R A                  :::::::::::::
    //:::::                                                         :::::::::::::
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    @Override
    public void addNode(IminerRemote ir) throws RemoteException {
        //se a rede não tiver o no
        if (!network.contains(ir)) {
            //adicionar o mineiro 
            network.add(ir);
            //espalhar o mineiro pela rede
            //para cada no remoto
            for (IminerRemote remoteName : network) {
                //adicionar o novo no ao remoto
                remoteName.addNode(ir);
                //adicionar o this ao remoto
                remoteName.addNode(this);

            }
            synchronize();
            //:::::::::: Notificar o listener ::::::::::::::
            if (listener != null) {
                listener.onAddNode(this);
            }
        }
    }
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * compara se dois objetos remotos são igauis Necessário para verificar se o
     * nó está numa coleção
     *
     * @param remote
     * @return
     */
    @Override
    public boolean equals(Object remote) {
        try {
            //comparar o endereço dos objetos remotos
            return ((IminerRemote) remote).getAdress().equals(this.getAdress());
        } catch (RemoteException ex) {
            return false;
        }
    }

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * retorna a lista dos nos
     *
     * @return
     * @throws RemoteException
     */
    @Override
    public List<IminerRemote> getNetwork() throws RemoteException {
        //retornar a lista
        return network;
    }

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  
    //:::::                                                         :::::::::::::
    //:::::                M I N A R       B L O C O S 
    //:::::                                                         :::::::::::::
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::    
    /**
     * calculate the nonce of the mssage
     *
     * @param message messagem
     * @param zeros number of zeros
     * @return nonce
     * @throws RemoteException
     */
    @Override
    public int mine(String message, int zeros) throws RemoteException {
        try {
            //executar a mineração com o mineiro
            return miner.mine(message, zeros);
        } catch (Exception ex) {
            throw new RemoteException("Mine " + ex);
        }
    }

    /**
     * Start mining the message
     *
     * @param message message
     * @param zeros number of zeros
     * @throws RemoteException
     */
    @Override
    public void startMining(String message, int zeros) throws RemoteException {
        data = message;
        try {
            //se já estiver a minar - Não faz nada
            if (miner.isMining()) {
                return;
            }
            //colocar o mineiro a minar
            miner.startMining(message, zeros);
            //Por todos os mineiros da rede a minar
            for (IminerRemote node : network) {
                node.startMining(message, zeros);
            }
            //:::::::::: Notificar o listener ::::::::::::::
            if (listener != null) {
                listener.onStartMining(message, zeros);
            }
        } catch (Exception ex) {
            throw new RemoteException("Start Mining ", ex);
        }
    }

    /**
     * Stop mining the message
     *
     * @param nonce nonce of message
     * @throws RemoteException
     */
    @Override
    public void stopMining(int nonce) throws RemoteException {
        //se o mineiro estiver a minar
        if (miner.isMining()) {
            //parar o mineiro
            miner.stopMining(nonce);
        }
        //parar todos os mineiros da rede rede
        for (IminerRemote node : network) {
            //se o mineiro remoro estiver a minar
            if (!node.getAdress().equals(this.getAdress()) && node.isMining()) //parar o mineiro remoto
            {
                node.stopMining(nonce);
            }
        }
        //:::::::::: Notificar o listener ::::::::::::::
        if (listener != null) {
            listener.onStopMining(getAdress(), nonce);
        }
    }

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  
    //:::::                                                         :::::::::::::
    //:::::               E N C A P S U L A M E N T O 
    //:::::                                                         :::::::::::::
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * gets the nonce of miner
     *
     * @return
     * @throws RemoteException
     */
    @Override
    public int getNonce() throws RemoteException {
        return miner.getNonce();
    }

    /**
     * verify if miner is mining
     *
     * @return is mining
     * @throws RemoteException
     */
    @Override
    public boolean isMining() throws RemoteException {
        return miner.isMining();
    }

    @Override
    public String getHash() throws RemoteException {
        return miner.getHash();
    }

    @Override
    public int getTicketNumber() throws RemoteException {
        return miner.getTicket();
    }

    @Override
    public String getAdress() throws RemoteException {
        return this.address;
    }

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  
    //:::::                                                         :::::::::::::
    //:::::                       BLOCK CHAIN                       :::::::::::::
    //:::::                                                         :::::::::::::
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     *
     * @param voto
     * @throws java.rmi.RemoteException
     */
    @Override
    public void addBlock(Block bloco) throws RemoteException {
        //se a rede não tiver o bloco
        if (!chainVote.contains(bloco)) {
            //adicionar o mineiro
            chainVote.add(bloco);
            try {
                chainVote.save("VoteChain.txt");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(RemoteMiner.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (listener != null) {
                listener.updateVoters();
            }
        }
    }

    public void load() throws RemoteException, FileNotFoundException, Exception {
        chainVote.load("VoteChain.txt");
    }

    /**
     *
     * @return a blockchain dos votos vindo do ficheiro
     */
    @Override
    public Blockchain getBlockchainVote() throws RemoteException {
        return chainVote;
    }

    public void setBlockchainVote(Blockchain blockchain) throws RemoteException {
        this.chainVote = blockchain;
    }

    @Override
    public int getBlockchainSize() throws RemoteException {
        return chainVote.getSize();
    }

    @Override
    public void synchronize() throws RemoteException {
        IminerRemote bigger = this;
        for (IminerRemote node : network) {
            if (node.getBlockchainSize() > bigger.getBlockchainSize()) {
                bigger = node;
            }
        }

        try {
            if (bigger.getBlockchainSize() != this.getBlockchainSize()) {
                Blockchain blkTemp = bigger.getBlockchainVote();
                this.setBlockchainVote(blkTemp);
                this.chainVote.save("VoteChain.txt");
                //:::::::::: Notificar o listener ::::::::::::::
                if (listener != null) {
                    listener.updateVoters();
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(RemoteMiner.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
