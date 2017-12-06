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
    * @return a RECEIPT message if the message was a REQUEST, null if it was a 
    * RELEASE
    * @throws RemoteException 
    */
   public LamportMessage receive(LamportMessage message) throws RemoteException;
   
   /**
    * Lock the critical section. Stops the thread until the critical section
    * has been obtained.
    * @throws java.rmi.RemoteException
    */
   public void lock() throws RemoteException;
   
   /**
    * Unlock the critical section. Sends a message to all other Lamport 
    * applications to notify them from the release of the critical section.
    * @throws java.rmi.RemoteException
    */
   public void unlock() throws RemoteException;
   
   /**
    * Get the value of the shared variable
    * @return the value 
    * @throws RemoteException 
    */
   public int getSharedValue() throws RemoteException;
   
   /**
    * Set the value of the shared variable. The application needs to enter in critical section
    * and then notifies all the other Lamport applications of the change
    * @param sharedValue
    * @throws RemoteException 
    */
   public void setSharedValue(int sharedValue) throws RemoteException;
}
