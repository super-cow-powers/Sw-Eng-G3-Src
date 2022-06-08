package g3.project.elements;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Samuel Perry<ssp525@york.ac.uk>
 */

public class DocElementTest {

    static DocElement test;
    private static Integer TEST_ITERATIONS = 1000;

    public DocElementTest(){
    }


    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** DocElement class tests ***");

    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("DocElement class tests complete.");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("New test running...");
        test = new DocElement("name");
    }

    @AfterEach
    public void tearDown() { System.out.println("Test complete."); }

    /**
     * Test of getNewUniqueID method, of class DocElement.
     */
    @Test
    public void testGetUniqueID(){
        System.out.println("Test: getUniqueID");
        String previous = "Empty";
        String previous2 = "Empty";
        String previous3 = "Empty";
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            String current = test.getNewUniqueID("idForType");
            assertTrue(test.validateUniqueID(current));
            assertNotSame(previous, current);
            assertNotSame(previous2, current);
            assertNotSame(previous3, current);
            previous = current;
            previous2 = previous;
            previous3 = previous2;
        }
    }

    /**
     * Test of addScriptFile method, of class DocElement.
     */
    @Test
    public void testAddScriptFile() throws Exception{
        System.out.println("Test: addScriptFile");

    }
}


