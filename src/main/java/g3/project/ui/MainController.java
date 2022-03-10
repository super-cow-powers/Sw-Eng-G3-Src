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
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Text;
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
    private Ingestion ingest;
    private Document model;
    private Engine engine;
    private Scene scene;
    private final BlockingQueue<String> editedElementQueue = new LinkedBlockingQueue<String>(); //UI has edited an element
    private final BlockingQueue<String> newElementQueue = new LinkedBlockingQueue<String>(); //UI requests element is created
    private final BlockingQueue<String> redrawElementQueue = new LinkedBlockingQueue<String>(); //Engine requests on-screen element be updated
    
    private final BlockingQueue<DocElement> docQueue = new LinkedBlockingQueue<DocElement>(); //Operate on this document
    private boolean darkMode = false;

    @FXML
    private MenuBar menuBar;

    @FXML
    private SplitPane splitPane;

    @FXML
    private Pane pagePane;

    /**
     * Handle action related to "About" menu item.
     *
     * @param event Event on "About" menu item.
     */
    @FXML
    private void handleAboutAction(final ActionEvent event) {
        newElementQueue.offer("Hello from the other-side");
    }

    /**
     * Handle action related to input (in this case specifically only responds
     * to keyboard event CTRL-A).
     *
     * @param event Input event.
     */
    @FXML
    private void handleKeyInput(final InputEvent event) {
        if (event instanceof KeyEvent) {
            final KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A) {
            }
        }
    }

    @FXML
    private void onGlobalKeyPress(final InputEvent event) {

    }

    @FXML
    private void onGlobalKeyRelease(final InputEvent event) {

    }

    @FXML
    private void onGlobalKeyTyped(final InputEvent event) {

    }

    @FXML
    private void onPageZoom(final ZoomEvent event) {
        System.out.println("Page is trying to zoom");
    }

    @FXML
    private void onPageZoomStart(final ZoomEvent event) {

    }

    @FXML
    private void onPageZoomEnd(final ZoomEvent event) {

    }

    @FXML
    private void handleExitAction(final ActionEvent event) {
        System.out.print("Quitting\n");
        exit();
    }
    
    @FXML
    private void handleToggleDarkModeAction(final ActionEvent event) {
        darkMode = !darkMode;
        toggleDarkMode();
    }
    
    public void drawText(String text, Point2D pos){
        Label l = new Label();
        l.setText(text);
        l.relocate(pos.getX(), pos.getY());
        pagePane.getChildren().add(l);
        System.out.println("g3.project.ui.MainController.drawText()");
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
        Ingestion ingest = new Ingestion();
        engine = new Engine(editedElementQueue, newElementQueue, docQueue, this);
        engine.start();
        var init_model_opt = ingest.parseDocXML(xmlFile);
        if (init_model_opt.isPresent()) {
            model = init_model_opt.get();
        } else {
            //Oops, couldn't parse initial doc.
        }

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
        pagePane.setEffect(new DropShadow());
        toggleDarkMode();
    }
}
