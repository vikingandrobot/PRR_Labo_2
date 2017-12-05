package ch.heigvd.prr_labo02_rmi.lamport.rmi;

import ch.heigvd.prr_labo02_rmi.lamport.message.LamportMessage;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 */
public interface Lamport extends Remote {
   /**
    * Send a lamport message
    * @param message the message to send
    * @throws RemoteException 
    */
   public void send(LamportMessage message) throws RemoteException;
}
