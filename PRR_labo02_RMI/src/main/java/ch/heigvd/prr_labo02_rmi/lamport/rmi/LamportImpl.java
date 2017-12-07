/**
 * File: LamportImpl.java
 * Authors: Sathiya Kirushnapillai & Mathieu Monteverde
 * Date: 07.12.2017
 */

package ch.heigvd.prr_labo02_rmi.lamport.rmi;

import ch.heigvd.prr_labo02_rmi.lamport.message.LamportMessage;
import ch.heigvd.prr_labo02_rmi.lamport.time.LogicalClock;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LamportImpl is the implementation of the Lamport remote object. Its 
 * purpose is to store the shared integer value, and use the Lamport algorithm
 * to manage the critical section and to share the value across the different 
 * Lamport applications. 
 * 
 * Please refer to the Lamport interface definition for information
 * regarding overriden methods.
 */
public class LamportImpl implements Lamport {

   // RMI registry address and the id of the current Lamport application
   private final String rmiAddress;
   private final int id;

   // The logical clock the Lamport application will use
   private final LogicalClock clock;

   // Array of Lamport applications to communicate with
   private final Lamport[] lamportApplications;

   // Array of LamporMessage instances received (or emitted)
   private final LamportMessage[] lamportMessages;

   // The shared value
   private int sharedValue;

   /**
    * Constructor.
    *
    * @param rmiAddress the RMI registry address
    * @param numberOfApplications the number of Lamport application in use
    * @param id the id of this specific Lamport application
    */
   public LamportImpl(String rmiAddress, int numberOfApplications, int id) {
      // Save the RMI registry address and our application id
      this.rmiAddress = rmiAddress;
      this.id = id;
      
      // Create a clock
      clock = new LogicalClock();
      
      // Create the array of RMI distant Lamport objects
      lamportApplications = new Lamport[numberOfApplications];
      
      // Create the array of received  messages
      lamportMessages = new LamportMessage[numberOfApplications];
      
      // Initialize the array to an array of RELEASE messages
      for (int i = 0; i < lamportMessages.length; ++i) {
         lamportMessages[i] = new LamportMessage(
                 LamportMessage.Type.RELEASE, 
                 0, 
                 this.id, 
                 sharedValue
         );
      }
   }

   @Override
   public synchronized LamportMessage receive(LamportMessage message) throws RemoteException {
      // Update our clock
      clock.update(message.getTimeStamp());
      
      // Store the message (Which is either a RELEASE or a REQUEST)
      lamportMessages[message.getSender()] = message;
      
      // The response we will return
      LamportMessage response = null;

      // Answer according to the type of message
      if (message.getType() == LamportMessage.Type.REQUEST) {
         // Create a RECEIPT
         response = new LamportMessage(
                 LamportMessage.Type.RECEIPT,
                 clock.getTime(),
                 this.id,
                 sharedValue
         );
      } else if (message.getType() == LamportMessage.Type.RELEASE) {
         // Update the shared value
         setSharedValue(message.getSharedValue());
      }

      // If we are waiting for a critical section and have permission
      LamportMessage myLastMessage = lamportMessages[this.id];
      
      // Notify the application if it is waiting for critical section
      if (myLastMessage.getType() == LamportMessage.Type.REQUEST
              && criticalSectionPermission()) {
         this.notify();
      }
      
      return response;
   }
   
   @Override
   public void lock() throws RemoteException{
      // Request the critical section
      requestCriticalSection();

      // If we don't have the permission, we wait
      synchronized(this) {
         if (!criticalSectionPermission()) {
            try {
               this.wait();
            } catch (InterruptedException ex) {
               Logger.getLogger(LamportImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      }
   }

   @Override
   public void unlock() throws RemoteException {
      // Release the critical section and notify the other applications of the change
      releaseCriticalSection();  
   }

   @Override
   public synchronized int getSharedValue() throws RemoteException {
      return sharedValue;
   }

   @Override
   public synchronized void setSharedValue(int sharedValue) throws RemoteException {
      // Update the value
      this.sharedValue = sharedValue;
   }

   /**
    * Request the critical section. This method sends a message of type REQUEST
    * to every other Lamport application. It also takes advantage of the RMI
    * return values to receive the RECEIPT responses from each of the other
    * Lamport applications.
    *
    * @throws RemoteException
    */
   private void requestCriticalSection() throws RemoteException {
      // Tick the clock
      clock.tick();

      // Create a message of type request
      LamportMessage request = new LamportMessage(
              LamportMessage.Type.REQUEST,
              clock.getTime(),
              this.id,
              sharedValue
      );

      // Store the request
      synchronized(this) {
         lamportMessages[id] = request;
      }

      // Send the request to all the other Lamport applications
      for (int i = 0; i < lamportApplications.length; ++i) {
         // Do not send a request to ourself
         if (i != this.id) {
            // If we haven't asked the RMI registry for this Lamport application
            if (lamportApplications[i] == null) {
               try {
                  lamportApplications[i] = getLamportRmi(i);
               } catch (NotBoundException ex) {
                  Logger.getLogger(LamportImpl.class.getName()).log(Level.SEVERE, null, ex);
               }
            }

            // Send the request and receive the receipt
            LamportMessage receipt = lamportApplications[i].receive(request);

            // Update our logical clock
            clock.update(receipt.getTimeStamp());

            // Store the receipt
            synchronized(this) {
               if (lamportMessages[i].getType() != LamportMessage.Type.REQUEST) {
                  lamportMessages[i] = receipt;
               }
            }

         }
      }
   }

   /**
    * Release the critical section. This method sends a message of type RELEASE
    * to all the other Lamport applications.
    *
    * @throws RemoteException
    */
   public void releaseCriticalSection() throws RemoteException {
      // Create the RELEASE message to send
      LamportMessage release = new LamportMessage(
              LamportMessage.Type.RELEASE,
              clock.getTime(),
              id,
              getSharedValue()
      );
      
      synchronized(this) {
         lamportMessages[this.id] = release;
      }

      // Send the RELEASE to every other application
      for (int i = 0; i < lamportApplications.length; ++i) {
         if (i != this.id) {
            lamportApplications[i].receive(release);
         }
      }
   }

   /**
    * Checks if the Lamport application has the right to enter the critical
    * section by checking the time stamps of all stored messages from other
    * application.
    *
    * @return true if the current Lamport application last time stamp is older
    * than all the other time stamps.
    */
   private boolean criticalSectionPermission() {
      boolean permission = true;
      long myLastTimeStamp = lamportMessages[this.id].getTimeStamp();

      // Go through every stored message from other applications
      for (int i = 0; i < lamportMessages.length; ++i) {
         // Ignore our own message
         if (i != this.id) {
            // Get the other message time stamp
            long otherTimeStamp = lamportMessages[i].getTimeStamp();

            // Decide if we have permission to enter the critical section so far
            permission = permission && ((myLastTimeStamp < otherTimeStamp)
                    || (myLastTimeStamp == otherTimeStamp && this.id < i));

         }
      }
      
      // Return the resulting permission
      return permission;
   }

   /**
    * Get a Lamport RMI by id
    *
    * @param id the id of the Lamport application we want to get RMI for
    * @return the corresponding Lamport
    * @throws RemoteException
    * @throws NotBoundException
    */
   private Lamport getLamportRmi(int id) throws RemoteException, NotBoundException {
      return (Lamport) LocateRegistry.getRegistry(rmiAddress)
              .lookup("lamport-" + id);
   }
}
