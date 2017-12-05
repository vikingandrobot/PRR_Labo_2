package ch.heigvd.prr_labo02_rmi.lamport.rmi;

import ch.heigvd.prr_labo02_rmi.lamport.message.LamportMessage;
import java.rmi.RemoteException;

/**
 *
 */
public class LamportImpl implements Lamport {
   
   // The shared value
   private int sharedValue;

   @Override
   public void send(LamportMessage message) throws RemoteException {
      // TODO manage the LamportMessage and the Lamport algorithm
   }

   @Override
   public int getSharedValue() throws RemoteException {
      return sharedValue;
   }

   @Override
   public void setSharedValue(int sharedValue) throws RemoteException {
      
      // TODO implement the Lamport algorithm to notify the other Lamport applications
      this.sharedValue = sharedValue;
   }
}
