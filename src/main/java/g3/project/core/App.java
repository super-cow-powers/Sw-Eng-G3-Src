package g3.project.core;

import g3.project.ui.MainController;
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
    private static MainController mainUIController;

    @Override
    public void start(Stage stage) throws IOException {
        /*Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.out.println("Handler caught exception: "+throwable.getMessage());
        });*/
        scene = new Scene(loadFXML("main"), 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.out.println("Stage is closing");
        mainUIController.gracefulExit();
        // Save file
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent fx_ld = fxmlLoader.load();
        mainUIController = (MainController) fxmlLoader.getController();
        return fx_ld;
    }

    public static void main(String[] args) {
        launch();
    }

}
