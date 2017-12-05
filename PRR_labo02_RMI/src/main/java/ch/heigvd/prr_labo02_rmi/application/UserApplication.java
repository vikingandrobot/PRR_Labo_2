package ch.heigvd.prr_labo02_rmi.application;

import ch.heigvd.prr_labo02_rmi.lamport.message.LamportMessage;
import ch.heigvd.prr_labo02_rmi.lamport.rmi.Lamport;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for the user application to allow to read and modifiy
 * the shared value across the network.
 */
public class UserApplication {
   public static void main(String[] args) {
      
      Lamport server = null;
      
      try {
         System.out.println("Asking for the server...");
         server = (Lamport) LocateRegistry.getRegistry().lookup("server");
         System.out.println("Server received.");
         LamportMessage message = new LamportMessage(
                 LamportMessage.Type.REQUEST,
                 0,
                 0,
                 10
         );
         server.send(message);
         System.out.println("Message sent.");
      } catch (NotBoundException ex) {
         Logger.getLogger(UserApplication.class.getName()).log(Level.SEVERE, null, ex);
      } catch (RemoteException ex) {
         Logger.getLogger(UserApplication.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
}
