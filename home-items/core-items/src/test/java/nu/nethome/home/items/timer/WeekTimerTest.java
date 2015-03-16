/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.nethome.home.items.timer;

import com.jtheory.jdring.AlarmEntry;
import java.util.LinkedList;
import nu.nethome.home.system.HomeService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author peter
 */
// @Ignore
public class WeekTimerTest {
    
    public WeekTimerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getModel method, of class WeekTimer.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        WeekTimer instance = new WeekTimer();
        String expResult = "<?xml version = \"1.0\"?>  <HomeItem Class=\"WeekTimer\" Category=\"Timers\" >  <Attribute Name=\"WeekDayTimes\" Type=\"String\" Get=\"getWeekDayTimes\" 	Set=\"setWeekDayTimes\" />  <Attribute Name=\"WeekEndTimes\" Type=\"String\" Get=\"getWeekEndTimes\" 	Set=\"setWeekEndTimes\" />  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />  <Action Name=\"On\" 	Method=\"on\" />  <Action Name=\"Off\" 	Method=\"off\" /></HomeItem> ";
        String resultOld = instance.getModelOld();
        System.out.println(resultOld);
        String resultNew = instance.getModel();
        System.out.println(resultNew);
        resultNew = resultNew.replaceAll("[\\s\\n]", "");
        resultOld = resultOld.replaceAll("[\\s\\n]", "");
        System.out.println(resultOld);
        System.out.println(resultNew);
        assertTrue(resultOld.compareTo(resultNew) == 0);
    }

    /**
     * Test of activate method, of class WeekTimer.
     */
    @Test
    public void testActivate() {
        System.out.println("activate");
        HomeService server = null;
        WeekTimer instance = new WeekTimer();
        instance.activate(server);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stop method, of class WeekTimer.
     */
    @Test
    public void testStop() {
        System.out.println("stop");
        WeekTimer instance = new WeekTimer();
        instance.stop();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of performCommand method, of class WeekTimer.
     */
    @Ignore
    @Test
    public void testPerformCommand() {
        System.out.println("performCommand");
        String commandString = "";
        WeekTimer instance = new WeekTimer();
        instance.performCommand(commandString);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWeekDayTimes method, of class WeekTimer.
     */
    @Test
    public void testGetWeekDayTimes() {
        System.out.println("getWeekDayTimes");
        WeekTimer instance = new WeekTimer();
        String expResult = "";
        String result = instance.getWeekDayTimes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWeekDayTimes method, of class WeekTimer.
     */
    @Test
    public void testSetWeekDayTimes() {
        System.out.println("setWeekDayTimes");
        String WeekDayTimes = "";
        WeekTimer instance = new WeekTimer();
        instance.setWeekDayTimes(WeekDayTimes);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculateAlarmEntries method, of class WeekTimer.
     */
    @Ignore
    @Test
    public void testCalculateAlarmEntries() {
        System.out.println("calculateAlarmEntries");
        LinkedList<AlarmEntry> alarms = null;
        String timePeriodsString = "";
        int[] weekDays = null;
        WeekTimer instance = new WeekTimer();
        instance.calculateAlarmEntries(alarms, timePeriodsString, weekDays);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOnCommand method, of class WeekTimer.
     */
    @Test
    public void testGetOnCommand() {
        System.out.println("getOnCommand");
        WeekTimer instance = new WeekTimer();
        String expResult = "";
        String result = instance.getOnCommand();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setOnCommand method, of class WeekTimer.
     */
    @Test
    public void testSetOnCommand() {
        System.out.println("setOnCommand");
        String OnCommand = "";
        WeekTimer instance = new WeekTimer();
        instance.setOnCommand(OnCommand);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWeekEndTimes method, of class WeekTimer.
     */
    @Test
    public void testGetWeekEndTimes() {
        System.out.println("getWeekEndTimes");
        WeekTimer instance = new WeekTimer();
        String expResult = "";
        String result = instance.getWeekEndTimes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWeekEndTimes method, of class WeekTimer.
     */
    @Test
    public void testSetWeekEndTimes() {
        System.out.println("setWeekEndTimes");
        String WeekEndTimes = "";
        WeekTimer instance = new WeekTimer();
        instance.setWeekEndTimes(WeekEndTimes);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getActive method, of class WeekTimer.
     */
    @Test
    public void testGetActive() {
        System.out.println("getActive");
        WeekTimer instance = new WeekTimer();
        String expResult = "";
        String result = instance.getActive();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setActive method, of class WeekTimer.
     */
    @Test
    public void testSetActive() {
        System.out.println("setActive");
        String Active = "";
        WeekTimer instance = new WeekTimer();
        instance.setActive(Active);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOffCommand method, of class WeekTimer.
     */
    @Test
    public void testGetOffCommand() {
        System.out.println("getOffCommand");
        WeekTimer instance = new WeekTimer();
        String expResult = "";
        String result = instance.getOffCommand();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setOffCommand method, of class WeekTimer.
     */
    @Test
    public void testSetOffCommand() {
        System.out.println("setOffCommand");
        String OffCommand = "";
        WeekTimer instance = new WeekTimer();
        instance.setOffCommand(OffCommand);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of on method, of class WeekTimer.
     */
    @Ignore
    @Test
    public void testOn() {
        System.out.println("on");
        WeekTimer instance = new WeekTimer();
        instance.on();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of off method, of class WeekTimer.
     */
    @Ignore
    @Test
    public void testOff() {
        System.out.println("off");
        WeekTimer instance = new WeekTimer();
        instance.off();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
