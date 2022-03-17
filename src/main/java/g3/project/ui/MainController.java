/*
 * Copyright (c) 2022, David Miall<dm1306@york.ac.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the copyright holder nor the names of its contributors may
 *   be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package g3.project.ui;

import static javafx.application.Platform.exit;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import com.jthemedetecor.OsThemeDetector;
import g3.project.core.Engine;
import g3.project.elements.DocElement;
import g3.project.xmlIO.Ingestion;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.*;
import nu.xom.Document;
import nu.xom.ParsingException;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class MainController {

    private final OsThemeDetector detector = OsThemeDetector.getDetector();
    private Engine engine;
    private Scene scene;
    //Scene graph nodes hashed by their ID
    private HashMap<String, javafx.scene.Node> drawnElements;
    //Cache image bytes by location
    private HashMap<String, Image> loadedImages = new HashMap<>();

    private boolean darkMode = false;

    @FXML
    private MenuBar menuBar;

    @FXML
    private SplitPane splitPane;

    @FXML
    private FlowPane toolPane;

    @FXML
    private HBox cardSelBox;

    @FXML
    private Label messageLabel;

    @FXML
    private Pane pagePane;

    /**
     * Handle action related to "About" menu item.
     *
     * @param event Event on "About" menu item.
     */
    @FXML
    private void handleAboutAction(final ActionEvent event) {
        engine.offerEvent(event);
    }

    /**
     * Handle action related to input
     *
     * @param event Input event.
     */
    @FXML
    private void handleEvent(final InputEvent event) {
        System.out.println("g3.project.ui.MainController.handleKeyInput()");
        engine.offerEvent(event);
    }

    @FXML
    private void handleExitAction(final ActionEvent event) {
        System.out.print("Quitting\n");
    }

    @FXML
    private void handleOpenNewDoc(final ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open New Stack");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML", "*.xml"),
                new FileChooser.ExtensionFilter("SPRES", "*.spres")
        );
        File new_file = fileChooser.showOpenDialog(pagePane.getScene().getWindow());
        if (new_file != null) {
            engine.offerNewDoc(new_file);
        }
    }

    public void gracefulExit() {
        engine.stop();
        Platform.exit();
    }

    @FXML
    private void handleToggleDarkModeAction(final ActionEvent event) {
        darkMode = !darkMode;
        toggleDarkMode();
    }

    public void drawText(String text, Point2D pos) { //Test Method
        Label l = new Label();
        l.setText(text);
        l.setFont(new Font(30));
        l.relocate(pos.getX(), pos.getY());
        pagePane.getChildren().add(l);
        System.out.println("g3.project.ui.MainController.drawText()");
    }

    /**
     * Configure the page
     *
     * @todo: Allow multiple pages
     * @todo: Resize scroll pane when the page is rotated
     */
    public void configCard(Optional<SizeObj> size, Optional<Color> colour, String ID) {
        size.ifPresent(f -> {
            pagePane.setMaxHeight(f.getY());
            pagePane.setMinHeight(f.getY());
            pagePane.setMaxWidth(f.getX());
            pagePane.setMinWidth(f.getX());
            pagePane.setRotate(f.getRot());
        });
        colour.ifPresent(f -> {
            var col = String.format("#%02X%02X%02X",
                    (int) (f.getRed() * 255),
                    (int) (f.getGreen() * 255),
                    (int) (f.getBlue() * 255));
            pagePane.setStyle("-fx-background-color: " + col);
        });
        pagePane.setId(ID);
    }

    /**
     * Clear the page
     *
     * @todo: Allow multiple pages
     */
    public void clearCard(String ID) {
        pagePane.getChildren().clear();
        pagePane.setStyle("-fx-background-color: #FFFFFF");
    }

    public void addCardButton(String friendlyName, String ID, Integer number) {
        Button cardButton = new Button(friendlyName);
        cardButton.setMaxSize(150, 50);
        cardButton.setMinSize(50, 50);
        cardButton.setId(ID + "-jump-card-button");
        cardButton.setWrapText(false);
        cardButton.setOnAction(event -> {
            clearCard(pagePane.getId());
            engine.offerEvent(event);
        });
        cardSelBox.getChildren().add(cardButton);
    }

    public void addTool(String toolname, String toolID) {
        Button toolButton = new Button(toolname);
        toolButton.setMaxSize(75, 75);
        toolButton.setMinSize(50, 50);
        toolButton.setId(toolID);
        toolButton.setWrapText(false);
        toolButton.setOnAction(event -> {
            engine.offerEvent(event);
        });
        toolPane.getChildren().add(toolButton);
    }

    public void updateShape(String ID, String type, SizeObj size, LocObj loc) {

    }

    public void updateImage(String ID, SizeObj size, LocObj loc, String path) {
        Image im = null;
        if (loadedImages.containsKey(path) == false) { //Caching images
            im = new Image(path);
            loadedImages.put(path, im);
        } else {
            im = loadedImages.get(path);
        }
        updateImage(ID, size, loc, im);
    }

    private void updateImage(String ID, SizeObj size, LocObj loc, Image im) {
        ImageView imv = null;
        if (drawnElements.containsKey(ID)) {
            var im_el = drawnElements.get(ID);
            if (im_el instanceof ImageView) {
                imv = (ImageView) im_el;
            }
        } else {
            imv = new ImageView();
            drawnElements.put(ID, imv);
        }

        imv.setImage(im);
        pagePane.getChildren().add(imv);
    }

    public void showNonBlockingMessage(String message) {
        messageLabel.setText(message);
    }

    private void toggleDarkMode() {
        Style style;
        if (darkMode == true) {
            style = Style.DARK;
        } else {
            style = Style.LIGHT;
        }
        //containerPane.getStylesheets().clear();
        //containerPane.getStylesheets().add(style.getStyleStylesheetURL());
    }

    public void initialize() {
        //this.scene = contentPane.getScene();
        File xmlFile = new File("exampledoc.xml");
        drawnElements = new HashMap<>();
        engine = new Engine(this);

        darkMode = detector.isDark();
        detector.registerListener(isDark -> {
            Platform.runLater(() -> {
                darkMode = isDark;
                toggleDarkMode();
            });
        });
        //containerPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        splitPane.getDividers().get(0)
                .positionProperty()
                .addListener((obs, oldPos, newPos) -> {
                    if (newPos.doubleValue() > 0.30) {
                        splitPane.getDividers().get(0).setPosition(0.30);
                    }
                });
        Platform.runLater(() -> { //Run when initialised
            engine.start();
            engine.offerNewDoc(xmlFile);
        });
        pagePane.setEffect(new DropShadow());
        toggleDarkMode();
    }
}
