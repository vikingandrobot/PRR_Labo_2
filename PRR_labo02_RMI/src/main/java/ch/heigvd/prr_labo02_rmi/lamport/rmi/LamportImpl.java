package ch.heigvd.prr_labo02_rmi.lamport.rmi;

import ch.heigvd.prr_labo02_rmi.lamport.message.LamportMessage;
import java.rmi.RemoteException;

/**
 *
 */
public class LamportImpl implements Lamport {
   
   private int value;

   @Override
   public void send(LamportMessage message) throws RemoteException {
      value = message.getSharedValue();
      System.out.println("Value is : " + value);
   }

   public int getValue() {
      return value;
   }

   public void setValue(int value) {
      this.value = value;
   }
}
