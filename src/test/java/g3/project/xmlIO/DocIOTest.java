package g3.project.xmlIO;

import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class DocIOTest {

    static DocIO test;
    static String presFilePath;
    static String newPath;
    public DocIOTest(){
    }

    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** DocIo class tests ***");
        presFilePath = "testFile.txt";
        newPath = "testSaveFile";
        test = new DocIO(presFilePath);
    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("DocIo class tests complete.");
    }

    @BeforeEach
    public void setUp() { System.out.println("New test running..."); }

    @AfterEach
    public void tearDown() { System.out.println("Test complete."); }


    /**
     * Test of save method, of class DocIo.
     */
    @Test
    public void testSave() throws Exception {
        System.out.println("Test: save");
    }

    /**
     * Test of saveAs method, of class DocIo.
     */
     @Test
     public void testSaveAs() throws Exception {
     System.out.println("Test: saveAs");
     String expectedMessage;
     String actualMessage;
     try{
         test.saveAs(newPath);
     } catch(Exception e){
         expectedMessage = "class java.io.IOException";
         actualMessage = IOException.class.toString();
         assertEquals(expectedMessage.toString(), actualMessage.toString());
     }

     try{
         test.saveAs(newPath + ".zip");
     } catch(Exception e){
         expectedMessage = "class java.io.IOException";
         actualMessage = IOException.class.toString();
         assertEquals(expectedMessage.toString(), actualMessage.toString());
     }}

    /**
     * Test of isURiInternal method, of class DocIo.
     */
    @Test
    public void testIsURIInternal() {
        System.out.println("Test: isURiInternal");
        assertTrue(test.isUriInternal("internalPath"));
        assertFalse(test.isUriInternal("http://notAnInternalPath"));
        assertFalse(test.isUriInternal("file://notAnInternalPath)"));
    }


    /**
     * Test of getInternalResource method, of class DocIo.
     */
    @Test
    public void testGetInternalResource() {
        System.out.println("Test: getInternalResource");
    }


    /**
     * Test of retrieveDoc method, of class DocIo.
     */
    @Test
    public void testRetrieveDoc() {
        System.out.println("Test: getInternalResource");
    }



};




