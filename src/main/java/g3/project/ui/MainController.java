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

import g3.project.graphics.LocObj;
import g3.project.graphics.SizeObj;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import com.jthemedetecor.OsThemeDetector;
import g3.project.core.Engine;
import g3.project.graphics.ExtLine;
import g3.project.graphics.ExtPolygon;
import g3.project.graphics.ExtShape;
import g3.project.graphics.ExtShapeFactory;
import g3.project.graphics.FontProps;
import g3.project.graphics.StrokeProps;
import g3.project.graphics.StyledTextSeg;
import g3.project.graphics.VisualProps;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
     * Can the scene be edited?
     */
    private boolean amEditable = false;

    /**
     * App's Engine.
     */
    private Engine engine;
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

    /**
     * Shape factory.
     */
    private ExtShapeFactory extShapeFactory = new ExtShapeFactory();

    /**
     * Object drag delta.
     */
    private Point2D dragDelta;

    /**
     * IO Console.
     */
    private Console console;

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
    private ScrollPane cardSelPane;

    @FXML
    private VBox pageVBox;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem saveAsMenuItem;
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
        //event.consume(); //Don't pass to elements below!
        engine.offerEvent(event);
    }

    /**
     * Show the Python console.
     */
    @FXML
    public void showConsole() {
        console.show();
    }

    /**
     * Event handler for input events.
     */
    private final EventHandler<InputEvent> handleInput = evt -> handleEvent(evt);

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
     * Handle click on close menu item.
     *
     * @param event exit-click event.
     */
    @FXML
    private void handleCloseAction(final ActionEvent event) {
        engine.showStartScreen();
    }

    /**
     * Handle click on save menu item.
     *
     * @param event exit-click event.
     */
    @FXML
    private void handleSaveAction(final ActionEvent event) {
        engine.saveCurrentDoc();
    }

    /**
     * Handle click on save as menu item.
     *
     * @param event exit-click event.
     */
    @FXML
    private void handleSaveAsAction(final ActionEvent event) {
        showSavePicker();
    }

    /**
     * Handle user request to open new doc.
     *
     * @param event user-event.
     */
    @FXML
    private void handleOpenNewDoc(final ActionEvent event) {
        showDocPicker();
    }

    /**
     * Shows a new-doc file picker, then loads selected doc.
     */
    public void showDocPicker() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open New Stack");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SuperPres", "*.spres"),
                new FileChooser.ExtensionFilter("ZIP", "*.zip")
        );

        var newFile = fileChooser.showOpenDialog(pagePane.getScene().getWindow());
        if (newFile != null) {
            engine.offerNewDoc(newFile);
        }
    }

    /**
     * Shows a document save window, then saves the doc.
     */
    public void showSavePicker() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SuperPres", "*.spres"),
                new FileChooser.ExtensionFilter("ZIP", "*.zip")
        );

        var newFile = fileChooser.showSaveDialog(pagePane.getScene().getWindow());
        if (newFile != null) {
            if (!newFile.getName().endsWith(".spress")) {
                newFile = new File(newFile.getAbsolutePath() + ".spres");
            }
            engine.saveCurrentDocAs(newFile.getAbsolutePath());
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
     * Draw/Redraw shape on screen.
     *
     * @param shapeType Type of shape.
     * @param props Shape visual properties.
     * @param stroke Stroke properties.
     * @param text Text segments.
     * @param size Shape size.
     * @param loc Shape location.
     * @param segments Segments for a line or polygon.
     */
    public void updateShape(final String shapeType, final VisualProps props, final StrokeProps stroke,
            final ArrayList<StyledTextSeg> text, final Optional<SizeObj> size, final Optional<LocObj> loc, final ArrayList<Double> segments) {

        var id = (String) props.getProp(VisualProps.ID).get();

        var drawnShape = drawnElements.get(id);

//Get the shape. Either a new or existing one.
        Optional<ExtShape> maybeShape = (drawnShape == null)
                ? extShapeFactory.makeShape(ExtShapeFactory.ShapeType.valueOf(shapeType.toLowerCase())) : Optional.of((ExtShape) drawnShape);

        maybeShape.ifPresent(s -> {
            //Set-up the shape.
            drawnElements.put(id, s);
            if (drawnShape == null) {
                pagePane.getChildren().add(s);
            }
            s.setProps(props); //Must do this before relocating!
            s.setStroke(stroke);

            s.setId(id);

            if (text.size() > 0) {
                var textAlign = ((String) text.get(0).getStyle().getProp(FontProps.ALIGNMENT).get()).toUpperCase();
                var textVAlign = ((String) text.get(0).getStyle().getProp(FontProps.VALIGNMENT).get()).toUpperCase();
                s.setText(text, TextAlignment.valueOf(textAlign), Pos.valueOf(textVAlign));
            }
            try {
                if (s instanceof ExtPolygon) {
                    ((ExtPolygon) s).setPoints(segments);
                } else if (s instanceof ExtLine) {
                    ((ExtLine) s).setPoints(segments);
                }
            } catch (Exception ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Move and rotate after everything else is set.
            size.ifPresent(sz -> s.setSize(sz));
            loc.ifPresentOrElse(l -> {
                s.setViewOrder(l.getZ());
                var origin = l.getLoc();
                s.relocate(origin.getX(), origin.getY());
            },
                    () -> s.setViewOrder(0));
        });
    }

    /**
     * Remove a given element.
     *
     * @param id ID of element to remove.
     */
    public void remove(final String id) {
        if (this.drawnElements.contains(id)) {
            var obj = this.drawnElements.get(id);
            pagePane.getChildren().remove(obj);
        }
    }

    /**
     * Set the cursor type.
     *
     * @param cType Cursor.
     */
    public void setCursorType(final Cursor cType) {
        pagePane.getScene().getRoot().setCursor(cType);
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
        colour.ifPresent(c -> {
            pagePane.setStyle("-fx-background-color: \'" + c.toString() + "\';");
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
        cardButton.setFocusTraversable(false);
        //CHECKSTYLE:OFF
        cardButton.setMaxSize(150, 50);
        cardButton.setMinSize(50, 50);
        //CHECKSTYLE:ON
        cardButton.setId(id + "-jump-card-button");
        cardButton.setWrapText(false);
        cardButton.setOnAction(ev -> {
            clearCard(id);
            engine.gotoPage(id, true);
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
                Image im;
                var resOpt = engine.getDocIO().getResource(path);
                if (resOpt.isPresent()) {
                    im = new Image(new ByteArrayInputStream(resOpt.get()));
                } else {
                    im = loadingGif;
                }
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
        var origin = loc.getLoc();
        imv.relocate(origin.getX(), origin.getY());
        imv.setId(id);
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
        console.show();
        console.putMessage(message);
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

    public void toggleEditable(Boolean editable) {
        this.amEditable = editable;
        if (!editable) {
            setCursorType(Cursor.DEFAULT);
        }
    }

    /**
     * Initialise the main UI.
     */
    public void initialize() {
        //this.scene = contentPane.getScene();

        var loadingGifStr = MainController.class
                .getResourceAsStream("loading.gif");
        loadingGif = new Image(loadingGifStr);

        engine = new Engine(this);

        pagePane.setViewOrder(0);
        pagePane.getChildren()
                .addListener((Change<? extends Node> c) -> {
                    while (c.next()) {
                        if (c.wasPermutated()) {
                            for (int i = c.getFrom(); i < c.getTo(); ++i) {
                                //permutate
                            }
                        } else if (c.wasUpdated()) {
                            //update item
                        } else {
                            for (Node addedNode : c.getAddedSubList()) {
                                addedNode.addEventHandler(MouseEvent.ANY, (e) -> {
                                    var handle = true;

                                    if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                                        if (amEditable) {
                                            addedNode.relocate(e.getSceneX() + dragDelta.getX(), e.getSceneY() + dragDelta.getY());
                                        }
                                    } else if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                                        if (amEditable) {
                                            handle = false;
                                            addedNode.setCursor(Cursor.MOVE);
                                            dragDelta = new Point2D(addedNode.getLayoutX() - e.getSceneX(), addedNode.getLayoutY() - e.getSceneY());
                                        }
                                    } else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
                                        if (amEditable) {
                                            handle = false;
                                            addedNode.setCursor(Cursor.HAND);
                                            dragDelta = new Point2D(addedNode.getLayoutX() - e.getSceneX(), addedNode.getLayoutY() - e.getSceneY());
                                        }
                                    } else if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                                        if (amEditable) {
                                            handle = false;
                                            addedNode.setCursor(Cursor.HAND);
                                        } else {
                                            //addedNode.setCursor(Cursor.DEFAULT);
                                        }
                                    }

                                    if (handle) {
                                        handleEvent(e);
                                    }
                                    e.consume();
                                });

                                addedNode.addEventHandler(KeyEvent.ANY, (e) -> {
                                    handleEvent(e);
                                    e.consume();
                                });
                            }
                        }
                    }
                });

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
            pagePane.addEventHandler(MouseEvent.ANY, handleInput);
            pagePane.addEventHandler(KeyEvent.ANY, handleInput);
            pagePane.setFocusTraversable(true);
            console = new Console((Stage) pagePane.getScene().getWindow(), (s -> engine.evalPyStr(s)));
            pagePane.addEventFilter(KeyEvent.ANY, event -> {
                if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.UP || event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT) {
                    handleEvent(event);
                    event.consume();
                }
            });

            engine.start();
        });

        //Set handlers for shapes and text.
        extShapeFactory.setHrefClickHandler((ev) -> {
            handleEvent(ev);
        });
        extShapeFactory.setHrefHoverEnterHandler((ev) -> {
            handleEvent(ev);
        });
        extShapeFactory.setHrefHoverExitHandler((ev) -> {
            handleEvent(ev);
        });
        cardSelPane.setFocusTraversable(false);

        var ds = new DropShadow();
        pagePane.setEffect(ds);

        toggleDarkMode();
    }
}
