package ch.heigvd.prr_labo02_rmi.lamport.time;

/**
 * The LogicalClock class reprsents a logical clock allowing
 * to have time stamps that increase each time the getTime method
 * is called.
 * 
 * The clock starts at 0
 */
public class LogicalClock {
   
   private long time;
   
   /**
    * Start a logical clock at 1.
    */
   public LogicalClock() {
      time = 0;
   }
   
   /**
    * Get the time. Increments the time by one before returning the value.
    * @return the logical time
    */
   public synchronized long getTime() {
      return time;
   }
   
   /**
    * Tick the clock to increment the time. Increments the time by 1.
    */
   public synchronized void tick() {
      ++time;
   }
   
   /**
    * Update the logical clock according to the time of another logicial clock.
    * The method will update the time by keeping the max value of its current
    * time and the given time of the other logical clock plus one.
    * @param otherTime the time of another logical clock
    */
   public synchronized void update(long otherTime) {
      time = Math.max(time, otherTime) + 1;
   }
}
