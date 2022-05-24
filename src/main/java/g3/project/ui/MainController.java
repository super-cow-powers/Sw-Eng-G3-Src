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
import g3.project.core.Engine;
import g3.project.graphics.ExtLine;
import g3.project.graphics.ExtPolygon;
import g3.project.graphics.ExtShape;
import g3.project.graphics.ExtShapeFactory;
import g3.project.graphics.FontProps;
import g3.project.graphics.StrokeProps;
import g3.project.graphics.StyledTextSeg;
import g3.project.graphics.VisualProps;
import g3.project.playable.Player;
import g3.project.playable.PlayerFactory;
import g3.project.xmlIO.Io;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class MainController {

    /**
     * Can the scene be edited?
     */
    private final AtomicBoolean amEditable = new AtomicBoolean(false);

    /**
     * App's Engine.
     */
    private Engine engine;
    /**
     * Scale for zooming page.
     */
    private Scale viewportScale = new Scale(1, 1);

    /**
     * Media player factory.
     */
    private PlayerFactory playerFact = new PlayerFactory();

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
     * Not Found image.
     */
    private Image notFoundIm = null;

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
     * Handle an edit event (drag).
     *
     * @param mev Event.
     */
    private void handleDragEditEvent(final MouseEvent mev) {
        EventTarget target = mev.getTarget();
        if (amEditable.get()) {
            if (target instanceof Node) {
                ((Node) target).relocate(mev.getSceneX() + dragDelta.getX(), mev.getSceneY() + dragDelta.getY());
                engine.elementRelocated(((Node) target).getId(),
                        new LocObj(new Point2D(mev.getSceneX() + dragDelta.getX(), mev.getSceneY() + dragDelta.getY()), ((Node) target).getViewOrder()));
                System.out.println("Drag: " + mev);
                handleEvent(mev);
                mev.consume();
            }
        }
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
        playerFact.freeAll(); //Free all native resources.
        executorSvc.shutdown();
        Platform.exit();
    }

    /**
     * Draw/Redraw shape on screen.
     *
     * @param id Shape ID.
     * @param shapeType Type of shape.
     * @param stroke Stroke properties.
     * @param text Text segments.
     * @param segments Segments for a line or polygon.
     */
    public void updateShape(final String id, final String shapeType, final StrokeProps stroke,
            final ArrayList<StyledTextSeg> text, final ArrayList<Double> segments) {

        final var drawnShape = drawnElements.get(id);

//Get the shape. Either a new or existing one.
        Optional<ExtShape> maybeShape = (drawnShape == null)
                ? extShapeFactory.makeShape(ExtShapeFactory.ShapeType.valueOf(shapeType.toLowerCase())) : Optional.of((ExtShape) drawnShape);

        maybeShape.ifPresent(s -> {
            //Set-up the shape.
            drawnElements.put(id, s);
            if (drawnShape == null) {
                pagePane.getChildren().add(s);
            }
            s.setId(id);
            updateShapeStroke(id, stroke);
            updateShapeText(id, text);
            try {
                if (s instanceof ExtPolygon) {
                    ((ExtPolygon) s).setPoints(segments);
                } else if (s instanceof ExtLine) {
                    ((ExtLine) s).setPoints(segments);
                }
            } catch (Exception ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    /**
     * Change Shape stroke.
     *
     * @param id Shape ID.
     * @param props Stroke Props.
     */
    public void updateShapeStroke(final String id, final StrokeProps props) {
        var drawnShape = drawnElements.get(id);
        if (drawnShape != null) {
            ((ExtShape) drawnShape).setStroke(props);
        }
    }

    /**
     * Change a shape's colour.
     *
     * @param id Target ID.
     * @param col colour.
     */
    public void updateShapeColour(final String id, final Color col) {
        var drawnShape = drawnElements.get(id);
        if (drawnShape != null) {
            ((ExtShape) drawnShape).setFill(col);
        }
    }

    /**
     * Update the text on a shape.
     *
     * @param id Shape ID.
     * @param text Text.
     */
    public void updateShapeText(final String id, final ArrayList<StyledTextSeg> text) {
        var s = drawnElements.get(id);
        if (s != null) {
            if (text.size() > 0) {
                var textAlign = ((String) text.get(0).getStyle().getProp(FontProps.ALIGNMENT).get()).toUpperCase();
                var textVAlign = ((String) text.get(0).getStyle().getProp(FontProps.VALIGNMENT).get()).toUpperCase();
                ((ExtShape) s).setText(text, TextAlignment.valueOf(textAlign), Pos.valueOf(textVAlign));
            }
        }
    }

    /**
     * Show/Hide an element.
     *
     * @param id Target ID.
     * @param visible Visibility.
     */
    public void setElementVisible(final String id, final Boolean visible) {
        var el = drawnElements.get(id);
        if (el != null) {
            el.setVisible(visible);
        }
    }

    /**
     * Moves the given element to the specified location.
     *
     * @param id Element ID.
     * @param loc Location to go to.
     */
    public void moveElement(final String id, final LocObj loc) {
        var el = drawnElements.get(id);
        if (el instanceof Visual) {
            el.relocate(loc.getLoc().getX(), loc.getLoc().getY());
            el.setViewOrder(loc.getZ());
        }
    }

    /**
     * Set props on Visual Element.
     *
     * @param id Element ID.
     * @param props Properties.
     */
    public void setElVisualProps(final String id, final VisualProps props) {
        var el = drawnElements.get(id);
        if (el instanceof Visual) {
            ((Visual) el).setProps(props);
        }
    }

    /**
     * Set a basic shadow on an element.
     *
     * @param id Target ID.
     * @param radius Shadow radius.
     */
    public void setElShadow(final String id, final Double radius) {
        var el = drawnElements.get(id);
        var ds = new DropShadow(radius, Color.BLACK);
        el.setEffect(ds);
    }

    /**
     * Resize a Visual Element.
     *
     * @param id Element ID.
     * @param size Element Size.
     */
    public void resizeElement(final String id, final SizeObj size) {
        var el = drawnElements.get(id);
        if (el instanceof Visual) {
            ((Visual) el).setSize(size);
        }
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
        //Some elements require cleanup - ffs.
        drawnElements.forEach((elid, node) -> {
            if (node instanceof Player) {
                ((Player) node).free();
            }
            drawnElements.remove(elid);
        });
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
     * Show some playable media.
     *
     * @param id media object ID.
     * @param path Path to media.
     * @param showPlayer Show player controls.
     * @param loopPlay Loop the media.
     * @param autoPlay Auto-play the media.
     * @param seekOffset Start seek offset.
     */
    public void showPlayable(final String id, final String path, final Boolean showPlayer, final Boolean loopPlay, final Boolean autoPlay, final Double seekOffset) {
        var player = (Player) drawnElements.get(id);
        if (player == null) {
            final var newplayer = playerFact.newPlayer();
            drawnElements.put(id, newplayer);
            String loadPath = path;
            //Get a resource from the archive. This is typically slower, as the resource system will copy the resource out.
            if (Io.isUriInternal(path)) {
                var resMaybe = engine.getDocIO().getResourceTempPath(path);
                if (resMaybe.isPresent()) {
                    loadPath = resMaybe.get();
                } else {
                    return;
                }
            }
            if (loadPath.startsWith("file:")) {
                loadPath = loadPath.replace("file:", "");
                //Not quite correct resolution of '~' - most shells only accept it at the very start.
                loadPath = loadPath.replaceFirst("~", System.getProperty("user.home"));
            }
            newplayer.load(loadPath, seekOffset);

            pagePane.getChildren().add(newplayer);
            player = newplayer;
        }
        playerSetControls(id, showPlayer);
        player.setLoop(loopPlay);
        playerSetPlaying(id, autoPlay);
    }

    /**
     * Set play/pause on player.
     *
     * @param id Player.
     * @param playing Play/Pause.
     */
    public void playerSetPlaying(final String id, final Boolean playing) {
        var pl = drawnElements.get(id);
        if (pl instanceof Player) {
            var player = (Player) pl;
            if (playing) {
                player.play();
            } else {
                player.pause();
            }
        }
    }

    /**
     * Show/hide player controls.
     *
     * @param id Player.
     * @param shown Show controls?
     */
    public void playerSetControls(final String id, final Boolean shown) {
        var pl = drawnElements.get(id);
        if (pl instanceof Player) {
            var player = (Player) pl;
            if (shown) {
                player.showControls();
            } else {
                player.hideControls();
            }
        }
    }

    /**
     * Show or update image on screen. The image will be cached based on its'
     * path.
     *
     * @param id Image ID.
     * @param path Image Path/URL/URI.
     * @param refreshCache Should I refresh the cache?
     */
    public void drawImage(final String id, final String path, final Boolean refreshCache) {
        /* Check if image is cached already. */
        if (loadedImages.containsKey(path) && !refreshCache) {
            /* In Cache */
            var im = loadedImages.get(path);
            drawImage(id, im);

        } else {
            /* Not cached */
            drawImage(id, loadingGif); //Show loading GIF
            //Runnable to background-load then show image
            Runnable imageLoaderRunnable = () -> {
                Image im;
                var resOpt = engine.getDocIO().getResource(path);
                if (resOpt.isPresent()) {
                    im = new Image(new ByteArrayInputStream(resOpt.get()));
                } else {
                    //Image was not found
                    im = notFoundIm;
                }
                loadedImages.put(path, im);
                Platform.runLater(() -> {
                    /* Check if image should stll be visible */
                    if (drawnElements.containsKey(id)) {
                        drawImage(id, path, false);
                    }
                });
            };
            //Create and start thread for download
            Thread imLoadThread = new Thread(imageLoaderRunnable);
            imLoadThread.start();
        }
    }

    /**
     * Show or update image on screen.
     *
     * @param id Image ID.
     * @param im JFX Image.
     */
    private void drawImage(final String id, final Image im) {
        VisImageView imv = null;
        if (drawnElements.containsKey(id)) {
            var imEl = drawnElements.get(id);
            if (imEl instanceof VisImageView) {
                imv = (VisImageView) imEl;
            }
        } else {
            imv = new VisImageView();
            drawnElements.put(id, imv);
            pagePane.getChildren().add(imv);
        }
        imv.setImage(im);
        imv.setId(id);
        imv.setPreserveRatio(true);
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

    @FXML
    public void handleTogEdit() {
        toggleEditable(!amEditable.get());
    }

    public void toggleEditable(Boolean editable) {
        this.amEditable.set(editable);
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

        var notFoundImStr = MainController.class
                .getResourceAsStream("not-found.png");
        notFoundIm = new Image(notFoundImStr);

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
                                addedNode.addEventHandler(MouseEvent.MOUSE_DRAGGED, ev -> handleDragEditEvent(ev));
                                addedNode.addEventHandler(MouseEvent.ANY, (e) -> {
                                    if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                                        if (amEditable.get()) {
                                            addedNode.setCursor(Cursor.MOVE);
                                            dragDelta = new Point2D(addedNode.getLayoutX() - e.getSceneX(), addedNode.getLayoutY() - e.getSceneY());
                                        }
                                    } else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
                                        if (amEditable.get()) {
                                            addedNode.setCursor(Cursor.HAND);
                                            dragDelta = new Point2D(addedNode.getLayoutX() - e.getSceneX(), addedNode.getLayoutY() - e.getSceneY());
                                        }
                                    } else if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                                        if (amEditable.get()) {
                                            addedNode.setCursor(Cursor.HAND);
                                        }
                                    } else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
                                        if (amEditable.get()) {
                                            addedNode.setCursor(Cursor.DEFAULT);
                                        }
                                    }
                                    handleEvent(e);
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
    }
}
