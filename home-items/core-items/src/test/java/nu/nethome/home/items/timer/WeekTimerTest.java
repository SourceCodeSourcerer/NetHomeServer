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
        String resultOld = instance.getModelOld();
        // System.out.println(resultOld);
        String resultNew = instance.getModel();
        // System.out.println(resultNew);
        resultNew = resultNew.replaceAll("[\\s\\n]", "");
        resultOld = resultOld.replaceAll("[\\s\\n]", "");
        // System.out.println(resultOld);
        // System.out.println(resultNew);
        assertEquals(resultOld, resultNew);
    }

}
