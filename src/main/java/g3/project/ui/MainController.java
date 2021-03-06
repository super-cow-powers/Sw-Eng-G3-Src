/*
 * Copyright (c) 2022, Group 3
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
import g3.project.graphics.Props;
import g3.project.graphics.StrokeProps;
import g3.project.graphics.StyledTextSeg;
import g3.project.graphics.VisualProps;
import g3.project.playable.Player;
import g3.project.playable.PlayerFactory;
import g3.project.xmlIO.DocIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Group 3
 */
public final class MainController {

    /**
     * Icon target X.
     */
    final Double iconX = 50d;
    /**
     * Icon target Y.
     */
    final Double iconY = 50d;

    /**
     * Can the scene be edited?
     */
    private final AtomicBoolean amEditable = new AtomicBoolean(false);

    /**
     * App's Engine.
     */
    private Engine engine;

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
     * Bubble events or consume at source.
     */
    private final AtomicBoolean bubbleEvents = new AtomicBoolean(false);
    /**
     * IO Console.
     */
    private Console console;

    private static final Double MIN_POS_VAL = 0.15;

//CHECKSTYLE:OFF
    //FXML bound objects
    @FXML
    private MenuBar menuBar;

    @FXML
    private SplitPane splitPane;

    @FXML
    private VBox propPane;

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

    @FXML
    private Button newCardButton;
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
     * Set the scale-factor of the page/card.
     *
     * @param scaleValue Scale-factor to apply.
     */
    private void scaleCard() {
        var pageWidth = pagePane.getWidth();
        var pageHeight = pagePane.getHeight();
        var boxWidth = pageVBox.getWidth();
        var boxHeight = pageVBox.getHeight();
        var wScale = boxHeight / (pageHeight + 10d);
        var hScale = boxWidth / (pageWidth + 10d);
        var newScale = Math.min(wScale, hScale);
        pagePane.setScaleX(newScale);
        pagePane.setScaleY(newScale);
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
        Node dragTarget = (Node) target;
        while (!(dragTarget instanceof Visual) && (dragTarget.getParent() != null)) {
            dragTarget = dragTarget.getParent(); //Find the first Visual Element being dragged.
        }
        if (amEditable.get()) {
            dragTarget.relocate(mev.getSceneX() + dragDelta.getX(), mev.getSceneY() + dragDelta.getY());
            engine.elementRelocated(dragTarget.getId(),
                    new LocObj(new Point2D(mev.getSceneX() + dragDelta.getX(), mev.getSceneY() + dragDelta.getY()), dragTarget.getViewOrder()));
            handleEvent(mev);
            //updatePropsList(dragTarget);
            mev.consume();
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
            ((Visual) el).setVisualProps(props);
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
     * Remove an element by ID.
     *
     * @param id ID of element to remove.
     */
    public void remove(final String id) {
        if (this.drawnElements.containsKey(id)) {
            var obj = this.drawnElements.remove(id);
            pagePane.getChildren().remove(obj);
        }
    }

    /**
     * Remove an element.
     *
     * @param el element.
     */
    public void remove(final Node el) {
        drawnElements.remove(el.getId());
        pagePane.getChildren().remove(el);
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
        scaleCard();
    }

    /**
     * Clear the page/card.
     *
     * @param id page to clear
     */
    public void clearCard(final String id) {
        pagePane.getChildren().clear();
        pagePane.setStyle("-fx-background-color: #FFFFFF");
        //Some elements require cleanup - ffs.
        drawnElements.forEach((elid, node) -> {
            if (node instanceof Player) {
                playerFact.free((Player) node);
            }
            drawnElements.remove(elid);
        });
    }

    /**
     * Add navigation button for specified card/page.
     *
     * @param friendlyName Card human name.
     * @param id Card ID.
     */
    public void addCardButton(final String friendlyName, final String id) {
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
     * @param iconPath Path to tool icon.
     */
    public void addTool(final String toolname, final String toolID, final String iconPath) {
        Tooltip buttonTip = new Tooltip(toolname);
        final Button toolButton = new Button();
        Optional<byte[]> maybeImage = engine.getToolIO().getResource(iconPath);
        maybeImage.map(imB -> new Image(new ByteArrayInputStream(imB)))
                .map(im -> {
                    var imv = new VisImageView(im);
                    imv.setFitWidth(iconX);
                    imv.setFitHeight(iconY);
                    return imv;
                })
                .ifPresentOrElse(imv -> toolButton.setGraphic(imv),
                        () -> toolButton.setText(toolname));
        Tooltip.install(toolButton, buttonTip);
        //CHECKSTYLE:OFF
        toolButton.setMaxSize(75, 75);
        toolButton.setMinSize(iconX, iconY);
        //CHECKSTYLE:ON
        toolButton.setId(toolID);
        toolButton.setWrapText(false);
        toolButton.setOnMouseClicked(event -> {
            engine.activateTool(toolButton.getId());
        });
        toolButton.setFocusTraversable(false);
        toolPane.getChildren().add(toolButton);
    }

    /**
     * Generate props list for given props and node.
     *
     * @param props Properties.
     * @param node Node/Element.
     * @param setterCallback Callback when props need to be set.
     */
    private void makeProgramaticPropsList(final HashMap<String, Object> props, final Node node, final BiConsumer<HashMap<String, Object>, String> setterCallback) {
        final String nodeID = node.getId();
        for (var prop : props.keySet()) {
            Label propLabel = new Label(prop);
            Control propEntry;
            var propVal = props.get(prop);
            var propClass = propVal.getClass();

            if (propClass.equals(Color.class)) {
                propEntry = new ColorPicker((Color) propVal);
                ((ColorPicker) propEntry).setOnAction(ev -> {
                    props.put(propEntry.getId(), ((ColorPicker) propEntry).getValue());
                    setterCallback.accept(props, nodeID);
                });
            } else if (propClass.equals(Double.class)) {
                propEntry = new Spinner(-Double.MAX_VALUE, Double.MAX_VALUE, (Double) propVal, 1d);
                ((Spinner) propEntry).setEditable(true);
                ((Spinner) propEntry).valueProperty().addListener((obs, oldValue, newValue) -> {
                    props.put(propEntry.getId(), ((Spinner) propEntry).getValue());
                    setterCallback.accept(props, nodeID);
                });
            } else if (propClass.equals(Boolean.class)) {
                propEntry = new CheckBox();
                ((CheckBox) propEntry).setSelected((Boolean) propVal);
                ((CheckBox) propEntry).setOnAction(ev -> {
                    props.put(propEntry.getId(), ((CheckBox) propEntry).isSelected());
                    setterCallback.accept(props, nodeID);
                });
            } else { //Probably a String.
                propEntry = new TextField(propVal.toString());
                //CHECKSTYLE:OFF
                propEntry.setMaxWidth(200d);
                //CHECKSTYLE:ON
                if (prop.equals("ID")) { //ID needs the node removing and redrawing.
                    ((TextField) propEntry).setOnAction(ev -> {
                        props.put(propEntry.getId(), ((TextField) propEntry).getText());
                        remove(node);
                        setterCallback.accept(props, nodeID);
                    });
                } else {
                    ((TextField) propEntry).setOnAction(ev -> {
                        props.put(propEntry.getId(), ((TextField) propEntry).getText());
                        setterCallback.accept(props, nodeID);
                    });
                }
            }
            propEntry.setId(prop);
            HBox propBox = new HBox(propLabel, propEntry);
            //HBox.setHgrow(propBox, Priority.ALWAYS);
            propPane.getChildren().addAll(propLabel, propEntry);
        }
    }

    /**
     * Update the list of properties.
     *
     * @param node Node to show props for.
     */
    public void updatePropsList(final Node node) {
        propPane.getChildren().clear();
        propPane.setAlignment(Pos.TOP_CENTER);
        final String nodeID = node.getId();
        Label itemLabel = new Label(nodeID);
        propPane.getChildren().add(itemLabel);
        final HashMap<String, Object> props = engine.getElementProperties(nodeID);
        makeProgramaticPropsList(props, node, (p, nID) -> engine.updateProperties(p, nID));

        final Optional<HashMap<String, Object>> maybeTextProps = engine.getShapeTextProps(nodeID);
        final Optional<String> maybeText = engine.getShapeTextString(nodeID);
        maybeTextProps.ifPresent(tp -> {
            Label textSepLabel = new Label("Text");
            propPane.getChildren().add(textSepLabel);
            makeProgramaticPropsList(tp, node, (p, nID) -> engine.setShapeTextProps(nID, p));
            final TextArea textBox = new TextArea();
            //CHECKSTYLE:OFF
            textBox.setMaxWidth(200d);
            //CHECKSTYLE:ON
            propPane.getChildren().add(textBox);
            maybeText.ifPresent(t -> textBox.setText(t));
            Button setTextButton = new Button("Set Text");
            setTextButton.setOnAction(ev -> engine.setShapeTextString(nodeID, textBox.getText()));
            propPane.getChildren().add(setTextButton);
        });

        Button editScrButton = new Button("Edit Script");
        editScrButton.setOnMouseClicked(e -> {
            //Launch a new editor for the element.
            var ed = new Editor((Stage) pagePane.getScene().getWindow(),
                    engine.getElScript(nodeID),
                    engine.getElScriptLang(nodeID),
                    nodeID,
                    (String lang, String text) -> {
                        engine.setElScript(nodeID, lang, text);
                    });

        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnMouseClicked(e -> {
            remove(node);
            propPane.getChildren().clear(); //Clear props.
            engine.deleteElement(nodeID);
        });

        propPane.getChildren().addAll(new HBox(editScrButton), new HBox(deleteButton));
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
            if (DocIO.isUriInternal(path)) {
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
        player.setId(id);
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
     * Make a player play/pause.
     *
     * @param id Player ID.
     * @param play Play/Pause.
     */
    public void togglePlayerPlaying(final String id, final Boolean play) {
        var pl = drawnElements.get(id);
        if (pl instanceof Player) {
            var player = (Player) pl;
            if (play) {
                player.play();
            } else {
                player.pause();
            }
        }
    }

    /**
     * Set if elements can be focussed on.
     *
     * @param focus Allow element focus?
     */
    private void setElementsFocusable(final Boolean focus) {
        for (var ch : pagePane.getChildren()) {
            ch.setFocusTraversable(focus);
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
     * Allows editing elements - FXML.
     */
    @FXML
    public void handleTogEdit() {
        toggleEditable(!amEditable.get());
    }

    /**
     * Allows editing elements.
     *
     * @param editable Is editable?
     */
    public void toggleEditable(final Boolean editable) {
        this.amEditable.set(editable);
        if (!editable) {
            propPane.getChildren().clear();
            setCursorType(Cursor.DEFAULT);
        }
    }

    /**
     * Toggle if events are consumed at the source.
     *
     * @param bubble Bubble events?
     */
    public void toggleBubble(final Boolean bubble) {
        bubbleEvents.set(bubble);
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
                                            addedNode.requestFocus();
                                            updatePropsList(addedNode);
                                            e.consume();
                                        }
                                    } else if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
                                        if (amEditable.get()) {
                                            e.consume();
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
                                    if (!bubbleEvents.get()) { //Not bubbling events.
                                        e.consume();
                                    }
                                });
                                addedNode.setFocusTraversable(true);
                                addedNode.focusedProperty().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                                        if (newPropertyValue) {
                                            if (amEditable.get()) {

                                            }
                                        } else {
                                        }
                                    }
                                });
                                addedNode.addEventHandler(KeyEvent.ANY, (e) -> {
                                    handleEvent(e);
                                    e.consume();
                                });

                            }
                        }
                    }
                });

        splitPane.getDividers().get(0)
                .positionProperty()
                .addListener((obs, oldPos, newPos) -> {
                    if (newPos.doubleValue() > MIN_POS_VAL) {
                        splitPane.getDividers().get(0).setPosition(MIN_POS_VAL);
                    }
                });

        pagePane.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (newPropertyValue) {
                    if (amEditable.get()) {
                        updatePropsList(pagePane);
                    }
                } else {
                }
            }
        });
//Add a new card button
        newCardButton.maxHeightProperty().bind(cardSelPane.heightProperty());
        newCardButton.maxWidthProperty().bind(cardSelPane.heightProperty());
        newCardButton.setMinSize(iconX, iconY);
        newCardButton.setPrefSize(iconX, iconY);
        var plusImageStr = MainController.class
                .getResourceAsStream("plus.jpg");
        var plusImageView = new VisImageView(new Image(plusImageStr));
        plusImageView.setFitWidth(iconX);
        plusImageView.setFitHeight(iconY);
        newCardButton.setGraphic(plusImageView);

        Platform.runLater(() -> { //Run when initialised
            pagePane.addEventHandler(MouseEvent.ANY, ev -> {
                if (ev.getEventType() == MouseEvent.MOUSE_CLICKED) {
                    pagePane.requestFocus();
                }
                handleInput.handle(ev);
            });
            pagePane.addEventFilter(KeyEvent.ANY, handleInput);
            pagePane.setFocusTraversable(true);
            console = new Console((Stage) pagePane.getScene().getWindow(), (s -> engine.consoleLineCallback(s)));

            engine.start();
            pageVBox.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                scaleCard();
            });
            pageVBox.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                scaleCard();
            });

        });

        //Set handlers for stuff.
        newCardButton.setOnMouseClicked(e -> {
            engine.makeNewCard();
        });

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

        pagePane.setFocusTraversable(true);
        pageVBox.setFocusTraversable(false);
        var ds = new DropShadow();
        pagePane.setEffect(ds);
    }

    /**
     * Return the Pane pagePane For testing.
     *
     * @return Pane containing all elements.
     */
    public Pane getPagePane() {
        return pagePane;
    }
}
