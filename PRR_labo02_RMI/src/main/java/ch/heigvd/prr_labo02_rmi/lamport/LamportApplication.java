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
   
   private Lamport lamport;

   private LamportApplication(String rmiAddress, int numberOfApplications, int id) 
           throws RemoteException, AlreadyBoundException {
      System.out.println("Starting the Lamport application...");
      lamport = (Lamport) UnicastRemoteObject.exportObject(new LamportImpl(
              rmiAddress,
              numberOfApplications,
              id
      ), 0);
      LocateRegistry.getRegistry().bind("lamport-" + id, lamport);
      System.out.println("Lamport application ready...");
   }

   public static void main(String[] args) {

      if (args.length != 3) {
         System.out.println("You must specify the RMI registry address, "
                 + "the number of Lamport applications running, and the ID "
                 + "of this Lamport application.");
         System.out.println("Parameters: ");
         System.out.println(" <RMI registry address>");
         System.out.println(" <number of Lamport applications>");
         System.out.println(" <application ID (integer >=0)>");
         System.exit(0);
      }

      String rmiAddress = args[0];
      int n = 0;
      int id = 0;
      try {
         n = Integer.parseInt(args[1]);
         id = Integer.parseInt(args[2]);
      } catch (NumberFormatException ex) {
         System.out.println("An error occured reading the number of applications "
                 + "or the application ID.");
         System.exit(0);
      }

      if (n <= 0 || id < 0) {
         System.out.println("The number of applications or the ID are not correct.");
         System.exit(0);
      }

      try {
         LamportApplication application = new LamportApplication(rmiAddress, n, id);
      } catch (RemoteException ex) {
         Logger.getLogger(LamportApplication.class.getName()).log(Level.SEVERE, null, ex);
      } catch (AlreadyBoundException ex) {
         Logger.getLogger(LamportApplication.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
}
