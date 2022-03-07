package g3.project.core;

import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import nu.xom.ParsingException;
import g3.project.xmlIO.Ingestion;


/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        /*Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.out.println("Handler caught exception: "+throwable.getMessage());
        });*/
        scene = new Scene(loadFXML("main"), 640, 480);
        
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        File xmlFile = new File(System.getProperty("user.dir") + "/sweng_21_PWS/example_doc.xml");
        Ingestion ingest = new Ingestion();
        try {
            ingest.parseDocXML(xmlFile);
        } catch (ParsingException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        launch();
    }

}