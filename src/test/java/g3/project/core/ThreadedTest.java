package g3.project.core;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Samuel Perry<ssp525@york.ac.uk>
 */
public class ThreadedTest {

    Threaded test;
    static AtomicBoolean expected = new AtomicBoolean(false);

    public ThreadedTest(){
    }

    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** Threaded class tests ***");
        }


    @AfterAll
    public static void tearDownClass() {
        System.out.println("Thread class tests complete.");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("New test.");
        test = new Threaded() {
            @Override
            public void run() {

            }
        };
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Test complete.");
    }

    /**
     * Test of getRunning method, in the Threaded class.
     */
    @Test
    void testGetRunning() {
        System.out.println("Test: getRunning");
        String newExpected = expected.toString();
        String actual = test.getRunning().toString();
        assertEquals(newExpected, actual);
    }

    /**
     * Test of getSusupended method, in the Threaded class.
     */
    @Test
    void testGetSuspended() {
        System.out.println("Test: getSuspended");
        String newExpected = expected.toString();
        String actual = test.getSuspended().toString();
        assertEquals(newExpected, actual);
    }

}
