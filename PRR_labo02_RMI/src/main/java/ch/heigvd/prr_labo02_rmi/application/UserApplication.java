/**
 * File: UserApplication.java
 * Authors: Sathiya Kirushnapillai & Mathieu Monteverde
 * Date: 07.12.2017
 */

package ch.heigvd.prr_labo02_rmi.application;

import ch.heigvd.prr_labo02_rmi.lamport.rmi.Lamport;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for the user application to allow to read and modifiy the shared
 * value across the network. 
 * 
 * The application offers a command line interface to read and write to the 
 * shared value. It offers the four following commands:
 * 
 * get  : Read the value
 * set  : Set the new value (you will then be asked to enter the new value)
 * test : Start n incrementations of the value (you will be asked to specify n)
 * quit : Quit the application
 * 
 * To start this application you should specify the address of the RMI registry
 * and the id of the Lamport application to use as command line parameters.
 * Example:
 * java -jar <application.jar> <RMI address> <id of the Lamport application>
 * 
 * Concerning the test command:
 * The purpose is to start at least two Lamport applications and two user 
 * applications and start the test on each user application. If the last 
 * one of the applications to finish displays a value corresponding to the sum
 * of all tests, the Lamport application should work. 
 * 
 * Please refer the LamportApplication to read about the tests we made and 
 * our conclusion on our Lamport application.
 */
public class UserApplication {
   
   // The Lamport application to use
   private Lamport lamport;
   
   /**
    * Constructor
    * 
    * @param rmiAddress the address of the RMI registry
    * @param lamportApplicationID the id of the Lamport application
    * @throws NotBoundException 
    * @throws RemoteException 
    */
   public UserApplication(String rmiAddress, int lamportApplicationID)
           throws NotBoundException, RemoteException {
      lamport = null;

      System.out.println("Asking the RMI registry for the "
              + "lamport application of ID " + lamportApplicationID);
      lamport = (Lamport) LocateRegistry
              .getRegistry(rmiAddress)
              .lookup("lamport-" + lamportApplicationID);
      System.out.println("Lamport application received from RMI registry.");
   }

   /**
    * Starts the application. The method allows the user to choose between
    * displaying the value of the shared variable and setting a new value for
    * this varaible and test the Lamport application.
    *
    * The method loops until the user enter the String 'quit'.
    */
   public void start() {

      Scanner in = new Scanner(System.in);
      String userInput = null;
      do {
         // display available commands
         displayCommands();
         
         // Read command
         userInput = in.nextLine();

         switch (userInput) {
            case "get":
               try {
                  // Get the value and give feedback
                  int value = lamport.getSharedValue();
                  System.out.println("The value is : " + value);
               } catch (RemoteException ex) {
                  System.out.println("An error occurred...");
                  Logger.getLogger(UserApplication.class.getName()).log(Level.SEVERE, null, ex);
               }
            break;

            case "set":
               try {
                  // Ask for the next value
                  System.out.println("Please enter the new value: ");
                  int newValue = in.nextInt();
                  
                  // Make the change
                  lamport.lock();
                  int value = lamport.getSharedValue();
                  System.out.println("The value before is : " + value);
                  System.out.println("Setting the new value...");
                  lamport.setSharedValue(newValue);
                  value = lamport.getSharedValue();
                  lamport.unlock();
                  
                  // Give feedback
                  System.out.println("The new value is : " + value);
                  in.nextLine();
                  
               } catch (InputMismatchException ex) {
                  System.out.println("Please enter an integer value.");
               } catch (RemoteException ex) {
                  System.out.println("An error occurred...");
                  Logger.getLogger(UserApplication.class.getName()).log(Level.SEVERE, null, ex);
               }
               // Set the value
               break;
            
            case "test":
               try {
                  // Ask for the number of tests
                  System.out.println("Please enter the number of tests: ");
                  int numberOfTests = in.nextInt();
                  
                  // Do the tests
                  for (int i = 0; i < numberOfTests; ++i) {
                     lamport.lock();
                     lamport.setSharedValue(lamport.getSharedValue() + 1);
                     lamport.unlock();
                  }
                  
                  // Give the feedback
                  System.out.println("Tests finished...");
                  lamport.lock();
                  System.out.println("The resulting value is: " 
                          + lamport.getSharedValue());
                  lamport.unlock();
                  
                  in.nextLine();
               } catch (InputMismatchException ex) {
                  System.out.println("Please enter an integer value.");
               } catch (RemoteException ex) {
                  System.out.println("An error occurred...");
                  Logger.getLogger(UserApplication.class.getName()).log(Level.SEVERE, null, ex);
               }
               break;

            case "quit":
               System.out.println("Exiting the application...");
               break;

            default:
               System.out.println("Unkown command, please try again...");
               break;
         }

      } while (!userInput.equals("quit"));

   }
   
   /**
    * Display the available commands.
    */
   public static void displayCommands() {
      System.out.println("====================================");
      System.out.println("Commands");
      System.out.println("'get' :   Get the value of the shared variable.");
      System.out.println("'set' :   Set the value of the shared variable. You "
              + "will be asked to enter the new desired value.");
      System.out.println("'test' :  Start a test program. You will be asked the "
              + "number of incrementations to do.");
      System.out.println("'quit' :  Quit the application.");
      System.out.println("====================================");
   }
   
   public static void main(String[] args) {

      // Reading the program arguments
      if (args.length != 2) {
         System.out.println("You must specify the RMI registry address and the "
                 + "ID of the Lamport application to use:");
         System.out.println("Usage: java -jar <application name> <RMI registry "
                 + "address> <Lamport application ID integer>");
         System.exit(0);
      }

      // Get the Lamport application ID and check its integrity
      int lamportApplicationID = 0;
      try {
         lamportApplicationID = Integer.parseInt(args[1]);
      } catch (NumberFormatException ex) {
         System.out.println("An error occured reading the Lamport application ID.");
         System.exit(0);
      }

      if (lamportApplicationID < 0) {
         System.out.println("The Lamport application ID must be equal or greater "
                 + "than 0.");
         System.exit(0);
      }

      String rmiAddress = args[0];

      try {
         // Create application
         UserApplication application = new UserApplication(rmiAddress, 
                 lamportApplicationID);
         // Start the application
         application.start();
      } catch (NotBoundException | RemoteException ex) {
         Logger.getLogger(UserApplication.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
}
