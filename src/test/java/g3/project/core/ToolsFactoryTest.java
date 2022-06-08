package g3.project.core;
import g3.project.ui.MainController;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Samuel Perry<ssp525@york.ac.uk>
 */
public class ToolsFactoryTest {
    static ToolsFactory test;
    public ToolsFactoryTest(){}

    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** ToolsFactory class tests ***");

    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("ToolsFactory class tests complete.");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("New test running...");
        test = new ToolsFactory();
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Test complete.");
    }

    /**
     * Test of startMakingElement method, in the ToolsFactory class.
     */
    @Test
    void testStartMakingElement() throws URISyntaxException {
        System.out.println("Test: startMakingElement");
        assertEquals(test.startMakingElement("Tools","http://pathhere").toString(), "[g3.project.core.Tools: Tools]");
        assertEquals(test.startMakingElement("Tool","http://pathhere").toString(), "[g3.project.core.Tool: Tool]");
        assertEquals(test.startMakingElement("Script","http://pathhere").toString(), "[g3.project.elements.ScriptElement: Script]");
        assertEquals(test.startMakingElement("Image","http://pathhere").toString(), "[g3.project.elements.ImageElement: Image]");
        assertEquals(test.startMakingElement("Other","http://pathhere").toString(), "[nu.xom.Element: Other]");

    }
}
