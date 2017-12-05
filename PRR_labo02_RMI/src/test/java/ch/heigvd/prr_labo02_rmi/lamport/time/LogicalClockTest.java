package ch.heigvd.prr_labo02_rmi.lamport.time;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for the LogicalClock class
 */
public class LogicalClockTest {
   
   private LogicalClock clock;
   
   public LogicalClockTest() {
   }
   
   @BeforeClass
   public static void setUpClass() {
   }
   
   @AfterClass
   public static void tearDownClass() {
   }
   
   @Before
   public void setUp() {
      // We test the clock when it starts at 0
      clock = new LogicalClock();
   }
   
   @After
   public void tearDown() {
   }

   /**
    * Test of getTime method, of class LogicalClock.
    */
   @Test
   public void getTimeShouldIncreaseTheTimeByOne() {
      long time = clock.getTime();
      clock.tick();
      assertEquals(time + 1, clock.getTime());
   }

   /**
    * Test of update method, of class LogicalClock.
    */
   @Test
   public void updateShouldTakeTheMaxBetweenTheTwoTimesPlusOne() {
      long time = clock.getTime();
      clock.update(time + 1);
      assertEquals(time + 2, clock.getTime());
      
      long time2 = clock.getTime();
      clock.update(time2 - 1);
      assertEquals(time2 + 1, clock.getTime());
      
      long time3 = clock.getTime();
      clock.update(time3);
      assertEquals(time3 + 1, clock.getTime());
      
   }
   
}
