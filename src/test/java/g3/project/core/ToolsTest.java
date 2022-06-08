package g3.project.core;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Samuel Perry<ssp525@york.ac.uk>
 */
public class ToolsTest {
    public ToolsTest(){}

    Tools newTools;

    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** Tools class tests ***");
    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("Tools class tests complete.");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("New test running...");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Test complete.");
        newTools = new Tools("name");
    }

    /**
     * Test of getTools method, in the Tools class.
     */
    @Test
    void testGetTools() {
        System.out.println("Test: getTools");
    }
}
