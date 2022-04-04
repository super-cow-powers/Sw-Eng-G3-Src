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

import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import com.jthemedetecor.OsThemeDetector;
import g3.project.core.Engine;
import g3.project.graphics.ExtShape;
import g3.project.graphics.FontProps;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import jfxtras.styles.jmetro.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class MainController {

    /**
     * Detects dark/light theme.
     */
    private final OsThemeDetector detector = OsThemeDetector.getDetector();
    /**
     * Is dark-mode enabled?
     */
    private boolean darkMode = false;

    /**
     * App's Engine.
     */
    private Engine engine;
    /**
     * Main scene.
     */
    private Scene scene;
    /**
     * Scale for zooming page.
     */
    private Scale viewportScale = new Scale(1, 1);

    /**
     * Scene graph nodes hashed by their ID.
     */
    private ConcurrentHashMap<String, javafx.scene.Node> drawnElements = new ConcurrentHashMap<>();

    /**
     * Cache image bytes by location.
     */
    private ConcurrentHashMap<String, Image> loadedImages = new ConcurrentHashMap<>();

    /**
     * Loading image.
     */
    private Image loadingGif = null;

    /**
     * Task Scheduler.
     */
    private final ScheduledExecutorService executorSvc = Executors.newSingleThreadScheduledExecutor();

    /**
     * Non-blocking message clear future.
     */
    private ScheduledFuture nbMessageClearFuture;

//CHECKSTYLE:OFF
    //FXML bound objects
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

    @FXML
    private ScrollPane pageScroll;

    @FXML
    private VBox pageVBox;
//CHECKSTYLE:ON
    /**
     * Duration for to show a non-blocking message.
     */
    private static final Long MESSAGE_DURATION = 6000L;
    /**
     * Duration message fade.
     */
    private static final Double NBMESSAGE_FADE_MS = 500d;

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
     * Handle action related to input.
     *
     * @param event Input event.
     */
    @FXML
    private void handleEvent(final InputEvent event) {
        System.out.println("g3.project.ui.MainController.handleKeyInput()");
        engine.offerEvent(event);
    }

    /**
     * Handle scroll event on page.
     *
     * @param event scroll event
     */
    private void pageScrollEventHandler(final ScrollEvent event) {
        final double deltaMult = 0.01;
        //Check that ctrl is pressed, and there is a delta
        if (event.isControlDown() && (event.getDeltaY() != 0)) {
            System.out.println("Delta:" + event.getDeltaY());
            var scaleValue = pagePane.getScaleX() + (event.getDeltaY() * deltaMult);
            setViewScale(scaleValue);
        }
        engine.offerEvent(event);
    }

    /**
     * Set the scale-factor of the page/card.
     *
     * @param scaleValue Scale-factor to apply.
     */
    public void setViewScale(final Double scaleValue) {
        pagePane.setScaleX(scaleValue);
        pagePane.setScaleY(scaleValue);
        pageVBox.setMinHeight(pageVBox.getHeight() * scaleValue);
        pageVBox.setMinWidth(pageVBox.getWidth() * scaleValue);
        System.out.println("Scale: " + pageVBox.getScaleX());
    }

    /**
     * Handle click on exit menu item.
     *
     * @param event exit-click event.
     */
    @FXML
    private void handleExitAction(final ActionEvent event) {
        System.out.print("Quitting\n");
    }

    /**
     * Handle user request to open new doc.
     *
     * @param event user-event.
     */
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
        var newFile = fileChooser.showOpenDialog(pagePane.getScene().getWindow());
        if (newFile != null) {
            engine.offerNewDoc(newFile);
        }
    }

    /**
     * Handle all the exit stuff.
     */
    public void gracefulExit() {
        engine.stop();
        executorSvc.shutdown();
        Platform.exit();
    }

    /**
     * Handle user action to toggle dark mode.
     *
     * @param event User action-event.
     */
    @FXML
    private void handleToggleDarkModeAction(final ActionEvent event) {
        darkMode = !darkMode;
        toggleDarkMode();
    }

    /**
     * TEST METHOD. Put plain text onto the screen.
     *
     * @param text Text to show.
     * @param pos Position to show it.
     */
    public void drawText(final String text, final Point2D pos) {
        //CHECKSTYLE:OFF
        Label l = new Label();
        l.setText(text);
        l.setFont(new Font(30));
        l.relocate(pos.getX(), pos.getY());
        pagePane.getChildren().add(l);
        System.out.println("g3.project.ui.MainController.drawText()");
        //CHECKSTYLE:ON
    }

    /**
     * Redraw shape on screen.
     *
     * @param id Shape ID
     * @param size Shape Size
     * @param loc Shape Location
     * @param shapeType Shape Type string
     * @param fillColour Shape Fill Colour
     * @param strokeColour Shape Stroke Colour
     * @param strokeWidth Shape Stroke Width
     * @param textString Shape Text String
     * @param textProps Shape Text Properties
     */
    public void updateShape(final String id, final SizeObj size, final LocObj loc, final String shapeType, final Color fillColour,
            final Color strokeColour, final Double strokeWidth, final String textString, final FontProps textProps) {
        ExtShape newShape = new ExtShape(shapeType, id, size.getX(), size.getY(), fillColour, strokeColour, strokeWidth, textString, textProps);
        newShape.setRotate(size.getRot());
        if (drawnElements.containsKey(id)) {
            pagePane.getChildren().remove(drawnElements.get(id));
        }
        drawnElements.put(id, newShape);
        var start = loc.getStart().get();
        newShape.relocate(start.getX(), start.getY());
        newShape.setViewOrder(loc.getZ());
        pagePane.getChildren().add(newShape);
    }

    /**
     * Configure the page/card.
     *
     * @param size Page size
     * @param colour Page colour
     * @param id Page ID
     */
    public void configCard(final Optional<SizeObj> size, final Optional<Color> colour, final String id) {
        /*
        @todo Allow multiple pages
        @todo Resize scroll pane when the page is rotated
         */
        size.ifPresent(f -> {
            pagePane.setMaxHeight(f.getY());
            pagePane.setMinHeight(f.getY());
            pagePane.setMaxWidth(f.getX());
            pagePane.setMinWidth(f.getX());
            pagePane.setRotate(f.getRot());
        });
        //CHECKSTYLE:OFF
        colour.ifPresent(f -> {
            var col = String.format("#%02X%02X%02X",
                    (int) (f.getRed() * 255),
                    (int) (f.getGreen() * 255),
                    (int) (f.getBlue() * 255));
            pagePane.setStyle("-fx-background-color: " + col);
        });
        //CHECKSTYLE:ON
        pagePane.setId(id);
    }

    /**
     * Clear the page/card.
     *
     * @param id page to clear
     * @todo: (Maybe) Support clearing specific cards.
     */
    public void clearCard(final String id) {
        pagePane.getChildren().clear();
        pagePane.setStyle("-fx-background-color: #FFFFFF");
        drawnElements.clear();
    }

    /**
     * Add navigation button for specified card/page.
     *
     * @param friendlyName Card human name.
     * @param id Card ID.
     * @param number Card sequence number.
     */
    public void addCardButton(final String friendlyName, final String id, final Integer number) {
        Button cardButton = new Button(friendlyName);
        //CHECKSTYLE:OFF
        cardButton.setMaxSize(150, 50);
        cardButton.setMinSize(50, 50);
        //CHECKSTYLE:ON
        cardButton.setId(id + "-jump-card-button");
        cardButton.setWrapText(false);
        cardButton.setOnAction(event -> {
            clearCard(pagePane.getId());
            engine.offerEvent(event);
        });
        cardSelBox.getChildren().add(cardButton);
    }

    /**
     * Remove all card nav buttons.
     */
    public void clearCardButtons() {
        cardSelBox.getChildren().clear();
    }

    /**
     * Add tool to tool-list.
     *
     * @param toolname Name of tool.
     * @param toolID Tool ID.
     */
    public void addTool(final String toolname, final String toolID) {
        Button toolButton = new Button(toolname);
        //CHECKSTYLE:OFF
        toolButton.setMaxSize(75, 75);
        toolButton.setMinSize(50, 50);
        //CHECKSTYLE:ON
        toolButton.setId(toolID);
        toolButton.setWrapText(false);
        toolButton.setOnAction(event -> {
            engine.offerEvent(event);
        });
        toolPane.getChildren().add(toolButton);
    }

    /**
     * Show or update image on screen.
     *
     * @param id Image ID.
     * @param size Image Size.
     * @param loc Image Location.
     * @param path Image Path/URL/URI.
     */
    public void updateImage(final String id, final SizeObj size, final LocObj loc, final String path) {
        /* Check if image is cached already. */
        if (!loadedImages.containsKey(path)) {
            /* Not cached */
            updateImage(id, size, loc, loadingGif); //Show loading GIF
            //Runnable to load then show image
            Runnable imageLoaderRunnable = () -> {
                var im = new Image(path);
                loadedImages.put(path, im);
                Platform.runLater(() -> {
                    /* Check if image should stll be visible */
                    if (drawnElements.containsKey(id)) {
                        updateImage(id, size, loc, path);
                    }
                });
            };
            //Create and start thread for download
            Thread imLoadThread = new Thread(imageLoaderRunnable);
            imLoadThread.start();
        } else {
            /* In Cache */
            var im = loadedImages.get(path);
            updateImage(id, size, loc, im);
        }
    }

    /**
     * Show or update image on screen. Private - bypasses cache.
     *
     * @param id Image ID.
     * @param size Image Size.
     * @param loc Image Location.
     * @param im JFX Image.
     */
    private void updateImage(final String id, final SizeObj size, final LocObj loc, final Image im) {
        ImageView imv = null;
        if (drawnElements.containsKey(id)) {
            var imEl = drawnElements.get(id);
            if (imEl instanceof ImageView) {
                imv = (ImageView) imEl;
            }
        } else {
            imv = new ImageView();
            drawnElements.put(id, imv);
            pagePane.getChildren().add(imv);
        }

        imv.setImage(im);
        if (loc.getStart().isPresent()) {
            var start = loc.getStart().get();
            imv.relocate(start.getX(), start.getY());
        }
        imv.setViewOrder(loc.getZ());
        imv.setRotate(size.getRot());
        imv.setPreserveRatio(true);
        imv.setFitHeight(size.getY());
        imv.setFitWidth(size.getY());

    }

    /**
     * Show a non-blocking message to the user.
     *
     * @param message message to show.
     */
    public void showNonBlockingMessage(final String message) {
        clearNBMessage(0);
        messageLabel.setText(message);
        messageLabel.setOpacity(1d);
        nbMessageClearFuture = executorSvc.schedule(() -> {
                Platform.runLater(() -> clearNBMessage(NBMESSAGE_FADE_MS));
        },
                MESSAGE_DURATION,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Show a blocking message to the user.
     *
     * @TODO Implement!!
     * @param message message to show.
     */
    public void showBlockingMessage(final String message) {
        showNonBlockingMessage(message);
    }

    /**
     * Clear non-blocking message area.
     *
     * @param msFade Fade Duration in mS.
     */
    private void clearNBMessage(final double msFade) {
        if (nbMessageClearFuture == null) {
            return;
        }
        if (!nbMessageClearFuture.isDone()) {
            nbMessageClearFuture.cancel(true);
        }
        FadeTransition ft = new FadeTransition(Duration.millis(msFade), messageLabel);
        ft.setFromValue(1d);
        ft.setToValue(0d);
        ft.play();
    }

    /**
     * Toggle dark mode.
     */
    private void toggleDarkMode() {
        Style style;
        if (darkMode) {
            style = Style.DARK;
        } else {
            style = Style.LIGHT;
        }
        //containerPane.getStylesheets().clear();
        //containerPane.getStylesheets().add(style.getStyleStylesheetURL());
    }

    /**
     * Initialise the main UI.
     */
    public void initialize() {
        //this.scene = contentPane.getScene();
        File xmlFile = new File("exampledoc.xml");
        var loadingGifPath = "file:".concat(MainController.class
                .getResource("loading.gif").getPath());
        loadingGif = new Image(loadingGifPath);

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
                    if (newPos.doubleValue() > 0.15) {
                        splitPane.getDividers().get(0).setPosition(0.15);
                    }
                });
        Platform.runLater(() -> { //Run when initialised
            engine.start();
            engine.offerNewDoc(xmlFile);
        });
        pagePane.setOnScroll((e) -> pageScrollEventHandler(e)); //Scaling stuff
        pageScroll.setOnKeyReleased((e) -> engine.offerEvent(e)); //I'll get the engine to handle the keys

        pageScroll.setPannable(true);
        pagePane.setEffect(new DropShadow());

        toggleDarkMode();
    }
}
