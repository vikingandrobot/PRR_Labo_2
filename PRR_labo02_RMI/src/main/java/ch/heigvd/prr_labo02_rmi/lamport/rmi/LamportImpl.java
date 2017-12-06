package ch.heigvd.prr_labo02_rmi.lamport.rmi;

import ch.heigvd.prr_labo02_rmi.lamport.message.LamportMessage;
import ch.heigvd.prr_labo02_rmi.lamport.time.LogicalClock;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
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
      this.rmiAddress = rmiAddress;
      this.id = id;

      clock = new LogicalClock();

      lamportApplications = new Lamport[numberOfApplications];
      lamportApplications[id] = this;

      lamportMessages = new LamportMessage[numberOfApplications];
   }

   @Override
   public LamportMessage send(LamportMessage message) throws RemoteException {
      // Store the message (Which is either a RELEASE or a REQUEST)
      synchronized(this) {
         lamportMessages[message.getSender()] = message;
      }
      // Update our clock
      clock.update(message.getTimeStamp());
      
      LamportMessage response = null;

      // Answer according to the type of message
      if (message.getType() == LamportMessage.Type.REQUEST) {
         // Give a RECEIPT
         response = new LamportMessage(
                 LamportMessage.Type.RECEIPT,
                 clock.getTime(),
                 this.id,
                 sharedValue
         );
      } else if (message.getType() == LamportMessage.Type.RELEASE) {
         // Update the shared value
         synchronized(this) {
            sharedValue = message.getSharedValue();
         }
      }

      // If we are waiting for a critical section and have permission
      LamportMessage myLastMessage;
      synchronized(this) {
         myLastMessage = lamportMessages[this.id];
      }
      if (myLastMessage != null
              && myLastMessage.getType() == LamportMessage.Type.REQUEST
              && criticalSectionPermission()) {
         synchronized(this) {
            this.notify();
         }
      }
      
      return response;
   }
   
   @Override
   public void lock() throws RemoteException{
      // Request the critical section
      requestCriticalSection();

      // If we don't have the permission, we wait
      if (!criticalSectionPermission()) {
         try {
            synchronized(this) {
               this.wait();
            }
         } catch (InterruptedException ex) {
            Logger.getLogger(LamportImpl.class.getName()).log(Level.SEVERE, null, ex);
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

      // Send a message of type request
      LamportMessage request = new LamportMessage(
              LamportMessage.Type.REQUEST,
              clock.getTime(),
              id,
              sharedValue
      );

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
            LamportMessage receipt = lamportApplications[i].send(request);

            // Update our logical clock
            clock.update(receipt.getTimeStamp());

            // Store the receipt
            synchronized(this) {
               if (lamportMessages[i] == null
                       || lamportMessages[i].getType() != LamportMessage.Type.REQUEST) {
                  lamportMessages[i] = receipt;
               }
            }

         }
      }

      // Store the request
      synchronized(this) {
         lamportMessages[id] = request;
      }
   }

   /**
    * Release the critical section. This method sends a message of type RELEASE
    * to all the other Lamport applications.
    *
    * @throws RemoteException
    */
   public void releaseCriticalSection() throws RemoteException {
      // Create the message to send
      LamportMessage release = new LamportMessage(
              LamportMessage.Type.RELEASE,
              clock.getTime(),
              id,
              sharedValue
      );

      // Send the RELEASE to every other application
      for (int i = 0; i < lamportApplications.length; ++i) {
         if (i != this.id) {
            lamportApplications[i].send(release);
         }
      }
      synchronized(this) {
         lamportMessages[this.id] = release;
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
   private synchronized boolean criticalSectionPermission() {
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

            // If the permission is already declined, we can stop
            if (permission == false) {
               break;
            }
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
   
   /**
    * Get the Logical clock the Lamport algorithm is using
    * @return the logical clock in use
    */
   public LogicalClock getClock() {
      return clock;
   }
   
}
