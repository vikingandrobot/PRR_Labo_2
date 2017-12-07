/**
 * File: Lamport.java
 * Authors: Sathiya Kirushnapillai & Mathieu Monteverde
 * Date: 07.12.2017
 */

package ch.heigvd.prr_labo02_rmi.lamport.rmi;

import ch.heigvd.prr_labo02_rmi.lamport.message.LamportMessage;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Lamport interface represents remote Lamport object that manage
 * the Lamport algorithm and share an integer value across the network.
 */
public interface Lamport extends Remote {
   
   /**
    * Receive a lamport message. This method is to be called by other Lamport
    * application to exchange messages and/or update the shared value. 
    * 
    * When the message type is a REQUEST, it should return a RECEIPT message and 
    * when it is of TYPE RELEASE, it should return null.
    * 
    * @param message the message to send
    * @return a RECEIPT message if the message was a REQUEST, null if it was a 
    * RELEASE
    * @throws RemoteException 
    */
   public LamportMessage receive(LamportMessage message) throws RemoteException;
   
   /**
    * Lock the critical section. Stops the thread until the critical section
    * has been obtained.
    * This method should be called by the user application.
    * 
    * @throws java.rmi.RemoteException
    */
   public void lock() throws RemoteException;
   
   /**
    * Unlock the critical section. This method sends a message to all other 
    * Lamport applications to notify them from the release of the critical 
    * section.
    * This method should be called by the user application.
    * 
    * @throws java.rmi.RemoteException
    */
   public void unlock() throws RemoteException;
   
   /**
    * Get the value of the shared variable. This method should be called by the 
    * user application.
    * 
    * @return the value 
    * @throws RemoteException 
    */
   public int getSharedValue() throws RemoteException;
   
   /**
    * Set the value of the shared variable. The user of this method should
    * use the lock method before and the unlock method after calling it.
    * 
    * @param sharedValue
    * @throws RemoteException 
    */
   public void setSharedValue(int sharedValue) throws RemoteException;
}
