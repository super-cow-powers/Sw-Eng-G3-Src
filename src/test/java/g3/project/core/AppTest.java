package g3.project.core;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 *
 * @author Samuel Perry<ssp525@york.ac.uk>
 */
public class AppTest {
    public AppTest() {}

    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** App class tests ***");
    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("App class tests complete.");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("New test running...");
    }

    @AfterEach
    public void tearDown() { System.out.println("Test complete."); }

    /**
     * Test of start method, in the app class.
     */
    @Test
    void testStart() {
        System.out.println("Test: start");
    }

    /**
     * Test of stop method, in the app class.
     */
    @Test
    void testStop() {
        System.out.println("Test: stop");
    }

    /**
     * Test of loadFXML method, in the app class.
     */
    @Test
    void testLoadFXML() {
        System.out.println("Test: loadFXML");
    }
}
