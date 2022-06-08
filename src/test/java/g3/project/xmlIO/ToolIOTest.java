package g3.project.xmlIO;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Samuel Perry<ssp525@york.ac.uk>
 */
public class ToolIOTest {

    static ToolIO test;
    static String toolFilePath = "filepath";

    public ToolIOTest(){

    }

    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** ToolIO class tests ***");

    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("ToolIO class tests complete.");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("New test running...");
        test = new ToolIO(toolFilePath);
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Test complete.");
    }

    /**
     * Test of canSave method, in the ToolIO class.
     */
    @Test
    void testCanSave() {
        System.out.println("Test: canSave");
        assertFalse(test.canSave());
    }

    /**
     * Test of isURiInternal method, of class ToolIO.
     */
    @Test
    public void testIsURIInternal() {
        System.out.println("Test11: isURiInternal");
        assertTrue(test.isUriInternal("internalPath"));
        assertFalse(test.isUriInternal("http://notAnInternalPath"));
        assertFalse(test.isUriInternal("file://notAnInternalPath)"));
    }





}
