package ch.heigvd.prr_labo02_rmi.lamport;

import ch.heigvd.prr_labo02_rmi.lamport.rmi.Lamport;
import ch.heigvd.prr_labo02_rmi.lamport.rmi.LamportImpl;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for the Lamport application.
 */
public class LamportApplication {

   public static void main(String[] args) {
      
      try {
         System.out.println("Starting the Lamport application...");
         Lamport lamport = (Lamport)UnicastRemoteObject.exportObject(new LamportImpl(), 0);
         LocateRegistry.getRegistry().bind("lamport-" + 0, lamport);
         System.out.println("Lamport application ready...");
      } catch (RemoteException ex) {
         Logger.getLogger(LamportApplication.class.getName()).log(Level.SEVERE, null, ex);
      } catch (AlreadyBoundException ex) {
         Logger.getLogger(LamportApplication.class.getName()).log(Level.SEVERE, null, ex);
      } 
   }
}
