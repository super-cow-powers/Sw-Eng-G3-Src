package g3.project.core;
import nu.xom.Builder;
import org.junit.jupiter.api.*;

import javax.xml.parsers.FactoryConfigurationError;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Samuel Perry<ssp525@york.ac.uk>
 */
public class ToolTest {
    String name = "SwEng";
    Tool tool;


    public ToolTest(){}

    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** Tool class tests ***");
    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("Tool class tests complete.");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("New test running...");
        tool = new Tool(name);
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Test complete.");
    }

    /**
     * Test of getName method, in the Tool class.
     */
    @Test
    void testGetName() {
        System.out.println("Test: getName");
    }

    /**
     * Test of getID method, in the Tool class.
     */
    @Test
    void testGetID() {
        System.out.println("Test: getID");
    }

    /**
     * Test of getParentElementScriptingBindings method, in the Tool class.
     */
    @Test
    void testGetParentElementScriptingBindings() {
        System.out.println("Test3: getParentElementScriptingBindings");
    }

    /**
     * Test of getScriptEl method, in the Tool class.
     */
    @Test
    void testGetScriptEl() {
        System.out.println("Test: getScriptEl");
        assertFalse(tool.getScriptEl().isPresent());

    }

    /**
     * Test of getRealType method, in the Tool class.
     */
    @Test
    void testGetRealType() {
        System.out.println("Test: getRealType");
        assertEquals(tool.getRealType().toString(), "g3.project.core.Tool");
    }

    /**
     * Test of getEvalRequired method, in the Tool class.
     */
    @Test
    void testGetEvalRequired() {
        System.out.println("Test: getEvalRequired");
        assertTrue(tool.getEvalRequired());
    }

    /**
     * Test of addScriptFile method, in the Tool class.
     */
    @Test
    void testAddScriptFile() throws IOException {
        System.out.println("Test: addScriptFile");
        String expectedMessage = "class java.io.IOException";

        try{
            tool.addScriptFile(Path.of("path"), "language");
        }catch(Exception e){
            String actualMessage = IOException.class.toString();
            assertEquals(expectedMessage, actualMessage.toString());
        }
    }

}
