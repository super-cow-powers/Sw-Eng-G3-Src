package g3.project.elements;//package g3.project.elements;

import g3.project.graphics.LocObj;
import g3.project.xmlIO.DocIO;
import nu.xom.Attribute;
import org.junit.jupiter.api.*;

import java.awt.geom.Point2D;
import java.util.Random;

import static g3.project.elements.VisualElement.EXT_URI;
import static org.junit.jupiter.api.Assertions.*;


 /**
 * @author Samuel Perry<ssp525@york.ac.uk>
 */

public class VisualElementsTest {

    static VisualElement test;
    static String name = "name";
    private static final Integer TEST_ITERATIONS = 1000;

    public VisualElementsTest(){}

    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** RecursiveBindings class tests ***");
    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("RecursiveBindings class tests complete.");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("New test running...");
        test = new VisualElement(name) {
            @Override
            public void delete(DocIO resIO) {

            }
        };
    }

    @AfterEach
    public void tearDown() { System.out.println("Test complete."); }

    /**
     * Test of setID and getID methods, of class VisualElements.
     * because setID calls getID they're tested together
     */
    @Test
    public void testSetID(){
        System.out.println("Test: setID");
        Random rand = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            String ID = rand.toString();
            test.setID(ID);
            String returnedID = test.getID();
            assertSame(returnedID,ID);
        }
    }

    /**
     * Test of setZInd and getZInd methods, of class VisualElements.
     * because setZInd calls getZInd they're tested together
     */
     @Test
     public void testSetZInd(){
         System.out.println("Test: setZInd");
         Random rand = new Random();
         for (int i = 0; i < TEST_ITERATIONS; i++) {
             Double Z = rand.nextDouble();
             test.setZInd(Z);
             Double returnedID = test.getZInd();
             assertEquals(returnedID.toString(),Z.toString());
         }
     }

    /**
     * Test of setFillColour methods, of class VisualElements.
     */
    @Test
    public void testSetFillColour() throws Exception{
        System.out.println("Test: setFillColour");
        Random rand = new Random();

        test.setFillColour("FFFFFF");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        { throw new IllegalArgumentException("Bad Colour String"); });
        assertEquals("Bad Colour String", exception.getMessage());
    }

     /**
      * Test of setOriginXY methods, of class VisualElements.
      */
    @Test
     public void testSetOriginXY(){
        System.out.println("Test: SetOriginXY");
        Random rand = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            javafx.geometry.Point2D point = new javafx.geometry.Point2D(rand.nextDouble(), rand.nextDouble());
            LocObj loc = new LocObj(point,rand.nextDouble());
            test.setOriginXY(loc);
            assertTrue(test.getOrigin().isPresent());
        }
    }

     /**
      * Test of makeAttrWithNS methods, of class VisualElements.
      */
     @Test
     public void testMakeAttrWithNS(){
         System.out.println("Test: makeAttrWithNS");

         String qualifiedName = "new:name";
         String attrVal = "attrVal";
         var nameSplit = qualifiedName.split(":");
         var attrNS = (nameSplit.length > 1) ? EXT_URI : "";
         var expected = new Attribute(qualifiedName, attrNS, attrVal);
         var real = test.makeAttrWithNS(qualifiedName,attrVal);

         assertEquals(expected.toString(), real.toString());


     }
}



