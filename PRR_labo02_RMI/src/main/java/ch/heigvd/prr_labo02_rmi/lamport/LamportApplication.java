/**
 * File: LamportApplication.java
 * Authors: Sathiya Kirushnapillai & Mathieu Monteverde
 * Date: 07.12.2017
 */

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
 * Main class for the Lamport application program. It creates the Lamport
 * remote object and registers it to the RMI registry.
 * 
 * To start the application you should first start the RMI registry from the 
 * root class folder using the command 'rmiregistry'.
 * 
 * Then you start this application by using the command: 
 * 
 * java -jar -Djava.rmi.server.codebase=file:<absolute path to the maven project>/target/classes/ 
 * target/lamport_application.jar <RMI registry address> <number of Lamport applications> 
 * <ID of the current application (starting at 0)>
 * 
 * This command comes from the official RMI documentation: 
 * https://docs.oracle.com/javase/7/docs/technotes/guides/rmi/hello/hello-world.html
 * 
 * You should start every Lamport appliation to before starting any of the user
 * application. Because of lack of time and because it wasn't explicitly
 * asked, the Lamport application only supports the case where one user
 * application is run per Lamport application.
 * 
 * Testing and results:
 * We used the test command available in the user application to test this 
 * Lamport application and we concluded that our Lamport implementation
 * doesn't work correctly. If we execute 10000 tests for example, the resulting
 * value doesn't equal 20000 but for example 19991. We see that we loose a few 
 * incrementations in the process and we haven't been able to locate the source
 * of the problem. 
 * 
 * On the other hand the values are shared across the network. We suppose it is 
 * a problem of mutual exclusion. 
 * 
 */
public class LamportApplication {
   
   // The Lamport remote object created
   private final Lamport lamport;
   
   /**
    * Constructor. Create the remote object and register it to the RMI registry
    * 
    * @param rmiAddress the address of the RMI registry
    * @param numberOfApplications the number of application in use
    * @param id the id of the current Lamport application
    * @throws RemoteException
    * @throws AlreadyBoundException 
    */
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
         System.out.println(" <application ID (integer >= 0)>");
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
      
      // Check as much integrity as possible
      if (n <= 0 || id < 0) {
         System.out.println("The number of applications or the ID are not correct.");
         System.exit(0);
      }

      try {
         // Start the application
         LamportApplication application = new LamportApplication(rmiAddress, n, id);
      } catch (RemoteException | AlreadyBoundException ex) {
         Logger.getLogger(LamportApplication.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
}
