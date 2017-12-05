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

   private final String rmiAddress;
   private final int numberOfApplications;
   private final int id;

   private final LogicalClock clock;

   private Lamport[] lamportApplications;

   private LamportMessage[] lamportMessages;

   private boolean sectionCritique;

   // The shared value
   private int sharedValue;

   public LamportImpl(String rmiAddress, int numberOfApplications, int id) {
      this.rmiAddress = rmiAddress;
      this.numberOfApplications = numberOfApplications;
      this.id = id;

      clock = new LogicalClock();

      lamportApplications = new Lamport[numberOfApplications];
      lamportApplications[id] = this;

      lamportMessages = new LamportMessage[numberOfApplications];
   }

   @Override
   public LamportMessage send(LamportMessage message) throws RemoteException {
      // TODO manage the LamportMessage and the Lamport algorithm

      return null;
   }

   @Override
   public int getSharedValue() throws RemoteException {
      return sharedValue;
   }

   @Override
   public void setSharedValue(int sharedValue) throws RemoteException {
      requestCriticalSection();
      
      // If we don't have the permission, we wait
      if (!criticalSectionPermission()) {
         
      } else {
        this.sharedValue = sharedValue;
      }
   }
   
   /**
    * Request the critical section 
    * @throws RemoteException 
    */
   private void requestCriticalSection() throws RemoteException {
      // We store the time of request
      long timeOfRequest = clock.getTime();
      
      // Send a message of type request
      LamportMessage request = new LamportMessage(
              LamportMessage.Type.REQUEST,
              timeOfRequest,
              id,
              sharedValue
      );

      // TODO send the request to all the other Lamport applications
      for (int i = 0; i < lamportApplications.length; ++i) {
         // If 
         if (i != this.id) {
            // If we haven't yet asked the Lamport RMI for this application
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
            if (lamportMessages[i] == null
                    || lamportMessages[i].getType() != LamportMessage.Type.REQUEST) {
               lamportMessages[i] = receipt;
            }

         }
      }
      
      // Store the request
      lamportMessages[id] = request;
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
      for (int i = 0; i < lamportMessages.length; ++i) {
         if (i != this.id) {
            // Get the other message time stamp
            long otherTimeStamp = lamportMessages[i].getTimeStamp();
            
            // Check if we have permission to enter the critical section so far
            permission = permission && (
                    (myLastTimeStamp < otherTimeStamp) ||
                    (myLastTimeStamp == otherTimeStamp && this.id < i)
                    );
            // If the permission is already false, we can stop
            if (permission == false)
               break;
         }
      }
      
      return permission;
   }
   
   /**
    * Get a Lamport RMI by id
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
