package ch.heigvd.prr_labo02_rmi.lamport.message;

import java.io.Serializable;

/**
 *
 */
public class LamportMessage implements Serializable {
   
   // The three types of lamport messages
   public static enum Type {REQUEST, RELEASE, RECEIPT};
   
   // The type of message
   private Type type;
   
   // The time stamp of the message
   private long timeStamp;
   
   // The sender ID
   private int sender;
   
   // The shared value passed along with the message
   private int sharedValue;
   
   /**
    * Default constructor.
    */
   public void LamportMessage() {}
   
   /**
    * Full constructor.
    * @param type the type of the message
    * @param timeStamp the time stamp of the message
    * @param sender the sender ID
    * @param sharedValue the shared value
    */
   public LamportMessage(Type type, long timeStamp, int sender, int sharedValue) {
      this.type = type;
      this.timeStamp = timeStamp;
      this.sender = sender;
      this.sharedValue = sharedValue;
   }

   public Type getType() {
      return type;
   }

   public void setType(Type type) {
      this.type = type;
   }

   public long getTimeStamp() {
      return timeStamp;
   }

   public void setTimeStamp(long timeStamp) {
      this.timeStamp = timeStamp;
   }

   public int getSender() {
      return sender;
   }

   public void setSender(int sender) {
      this.sender = sender;
   }

   public int getSharedValue() {
      return sharedValue;
   }

   public void setSharedValue(int sharedValue) {
      this.sharedValue = sharedValue;
   }
   
}
