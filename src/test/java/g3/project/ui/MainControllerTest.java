package g3.project.ui;

import javafx.geometry.Point2D;
import javafx.scene.Node;

import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MainControllerTest {
    
    public MainControllerTest() { 
    }
  
    @BeforeAll
    public static void setUClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Tested Here:
     * gracefulExit
     * drawText
     * configPage
     * clearPage
     * addTool
     * showNonBlockingMessage
     * initialize
     */

     /**
      * Test of gracefulExit, of class MainController
      */
     @Test
     public void testGracefulExit() throws Exception {
         System.out.println("gracefulExit");
         MainController instance = new MainController();
         //Run function to be tested
         instance.gracefulExit();
     }

     @Test
     public void testDrawText() throws Exception {
         System.out.println("drawText");
         Random rand = new Random();
        // Test variables
         String testText = "testText";
         Point2D testPos = new Point2D(rand.nextInt(), rand.nextInt());

         MainController instance = new MainController();
         // Run function to be tested
         instance.drawText(testText, testPos);

         // Testing position
         int size = instance.getPagePane().getChildren().size();
         Node lastChild = instance.getPagePane().getChildren().get(size);
         assertEquals(lastChild.getLayoutX(), testPos.getX());
         assertEquals(lastChild.getLayoutY(), testPos.getY());
         // Testing text
     }
}
