package g3.project.core;

import g3.project.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App.
 */
public class App extends Application {

    /**
     * Default Window X Size.
     */
    private static final int DEF_WINDOW_X_SIZE = 900;
    /**
     * Default Window Y Size.
     */
    private static final int DEF_WINDOW_Y_SIZE = 600;
    /**
     * Main Scene.
     */
    private static Scene scene;
    /**
     * Main UI controller.
     */
    private static MainController mainUIController;

    /**
     * Set Root FXML doc.
     *
     * @param fxml FXML Document name.
     * @throws IOException Couldn't load document.
     */
    static void setRoot(final String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Load the named FXML document.
     *
     * @param fxml FXML Document name.
     * @return JFX parent node.
     * @throws IOException Couldn't load document.
     */
    private static Parent loadFXML(final String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainController.class.getResource(fxml + ".fxml"));
        Parent fxld = fxmlLoader.load();
        mainUIController = fxmlLoader.getController();
        return fxld;
    }

    /**
     * Main entry point.
     *
     * @param args Command-line arguments.
     */
    public static void main(final String[] args) {
        launch();
    }

    @Override
    public final void start(final Stage stage) throws IOException {
        /*Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.out.println("Handler caught exception: "+throwable.getMessage());
        });*/
        scene = new Scene(loadFXML("main"), DEF_WINDOW_X_SIZE, DEF_WINDOW_Y_SIZE);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public final void stop() {
        System.out.println("Stage is closing");
        mainUIController.gracefulExit();
        // Save file
    }

}
