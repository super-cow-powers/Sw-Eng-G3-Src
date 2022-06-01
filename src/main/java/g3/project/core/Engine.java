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
package g3.project.core;

import g3.project.elements.*;
import g3.project.elements.DocElement;
import g3.project.elements.PageElement;
import g3.project.elements.VisualElement;
import g3.project.graphics.FontProps;
import g3.project.graphics.StyledTextSeg;
import g3.project.network.NetThing;
import g3.project.graphics.LocObj;
import g3.project.ui.MainController;
import g3.project.graphics.SizeObj;
import g3.project.graphics.StrokeProps;
import g3.project.graphics.VisualProps;
import g3.project.xmlIO.DocIO;
import g3.project.xmlIO.ToolIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javax.script.ScriptException;
import nu.xom.Element;

/**
 * @author david
 */
public final class Engine extends Threaded {

    /**
     * XML IO.
     */
    private DocIO docIO;

    /**
     * Tools IO.
     */
    private ToolIO toolIO;

    /**
     * Network Communications module.
     */
    private final NetThing netComms = new NetThing();

    /**
     * Scripting Engine Controller.
     */
    private Scripting scriptingEngine;

    /**
     * List of tools.
     */
    private Tools currentTools;

    /**
     * Document currently open.
     */
    private DocElement currentDoc;

    private String currentToolID = "";

    /**
     * Navigation history stack.
     */
    private final Stack<String> navHistory = new Stack<>();
    /**
     * Current line from console.
     */
    private volatile String consoleLine = new String();
    /**
     * Console is taking user input?
     */
    private final AtomicBoolean consoleOnUserInput = new AtomicBoolean(false);
    /**
     * Console has data?
     */
    private CountDownLatch consoleLineReady;
    /**
     * Executor service for delaying events.
     */
    private final ScheduledExecutorService elementDelayService = Executors.newScheduledThreadPool(1);

    /**
     * Event queue from input sources.
     */
    private final BlockingQueue<Event> eventQueue
            = new LinkedBlockingQueue<Event>(); // Something has happened
    /**
     * Document open-request queue.
     */
    private final BlockingQueue<File> docQueue
            = new LinkedBlockingQueue<File>();

    /**
     * Out of thread call queue.
     */
    private final BlockingQueue<Runnable> callQueue
            = new LinkedBlockingQueue<>();

    /**
     * Ref to the UI controller.
     */
    private final MainController controller;
    /**
     * Writer for the scripting engine output.
     */
    private final Writer scrWriter;

    /**
     * Filename for the start screen
     */
    private final String startScreenFileName = "start_screen.spres";

    /**
     * Filename for a new empty project
     */
    private final String emptyFileName = "empty.spres";

    /**
     * Get running
     */
    private AtomicBoolean running = getRunning();

    /**
     * Get suspended
     */
    private AtomicBoolean suspended = getSuspended();

    /**
     * Constructor.
     *
     * @param uiController Ref to the main UI controller.
     */
    public Engine(final MainController uiController) {
        super();
        this.controller = uiController;
        scrWriter = new Writer() {
            @Override
            public void write(final char[] chars, final int i, final int i1) throws IOException {
                var newChars = Arrays.copyOfRange(chars, i1, i1);
                putMessage(newChars.toString() + "\n", true);
            }

            @Override
            public void write(final String str) {
                putMessage(str, true);
            }

            @Override
            public void flush() throws IOException {
                putMessage("\n", Boolean.TRUE);
            }

            @Override
            public void close() throws IOException {

            }
        };

    }

    /**
     * Send an event to the engine.
     *
     * @param event Event to send.
     */
    public void offerEvent(final Event event) {
        eventQueue.offer(event);
        unsuspend();
    }

    /**
     * Send a doc to the engine.
     *
     * @param xmlFile Doc to open.
     */
    public void offerNewDoc(final File xmlFile) {
        docQueue.offer(xmlFile);
        unsuspend();
    }

    /**
     * Run a function on the engine thread.
     *
     * @param r Runnable function.
     */
    public void runFunction(final Runnable r) {
        callQueue.offer(r);
        unsuspend();
    }

    /**
     * Returns the current Document IO object.
     *
     * @return doc IO.
     */
    public DocIO getDocIO() {
        return docIO;
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {

        while (!(running.get())) {
        }
        try {
            //Start network thing
            netComms.start();
            //Init Scripting Engine
            scriptingEngine = new Scripting("python", this, scrWriter);
            scriptingEngine.setGlobal("document", currentDoc);
            //Show Start Screen
            showStartScreen();
            // Load in the tools
            loadTools()
                    .ifPresentOrElse(
                            t -> {
                                currentTools = t;
                                // Add tool buttons
                                var iterTool = t.getTools().iterator();
                                while (iterTool.hasNext()) {
                                    var currentTool = iterTool.next();
                                    try {
                                        scriptingEngine.evalElement(currentTool); //Evaluate/load tool's script.
                                    } catch (ScriptException | IOException ex) {
                                        Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    final String imagePath;
                                    var maybeImPath = currentTool.getImagePath();
                                    if (maybeImPath.isPresent()) {
                                        imagePath = maybeImPath.get();
                                    } else {
                                        imagePath = "";
                                    }
                                    Platform.runLater(() -> controller.
                                    addTool(currentTool.getName(), currentTool.getID(), imagePath));
                                }
                            },
                            () -> {
                                putMessage("Failed Loading Tools!", false);
                            });
        } catch (Exception ex) {
            //Something went wrong. Couldn't start.
            ex.printStackTrace();
            netComms.stop();
            return;
        }
        // Quit if running flag set to false
        while (running.get()) {
            try {
                if (!docQueue.isEmpty()) { //New doc request?
                    parseNewDoc(docQueue.take());
                } else if (!eventQueue.isEmpty()) { //New event?
                    handleEvent(eventQueue.take());
                } else if (!callQueue.isEmpty()) { //Out-of-thread call request
                    var callee = callQueue.take();
                    callee.run();
                } else { //Nothing to do. Suspend
                    suspended.set(true);
                }

                while (suspended.get() && running.get() && eventQueue.isEmpty()
                        && callQueue.isEmpty() && docQueue.isEmpty()) { // Suspend
                    synchronized (this) {
                        //CHECKSTYLE:OFF
                        wait(100, 0);
                        //CHECKSTYLE:ON
                    }
                }
            } catch (Exception ex) {
                //Something went wrong.
                ex.printStackTrace();
            }
        }
        netComms.stop();
        if (docIO != null) {
            docIO.close(); //Cleanup resources
        }
        System.out.println("Engine is going down NOW.");
        elementDelayService.shutdown();
        return;
    }

    /**
     * Handle an incoming event.
     *
     * @param event Event to handle.
     */
    private void handleEvent(final Event event) {
        var evSrc = event.getSource();
        if (evSrc instanceof Button) {
            handleButtonEvent(event);
        } else if (evSrc instanceof javafx.scene.Node) {
            //Pass to tools, then to elements if the event wasn't sunk by a tool.
            routeToolEvent(event).ifPresent(e -> routeElementEvent(e));
        }
    }

    /**
     * Route an event on an element to the correct place.
     *
     * @param ev event.
     */
    private void routeElementEvent(final Event ev) {

        final var evSrc = (javafx.scene.Node) ev.getSource();
        var elID = evSrc.getId();

        if (elID != null) { //Element has an ID
            //Route event to element. Only route to scriptable ones.
            currentDoc.getElementByID(elID)
                    .filter(el -> el instanceof Scriptable)
                    .ifPresent(el -> {
                        if (ev instanceof MouseEvent) {
                            routeMouseEvent((MouseEvent) ev, el);
                        } else if (ev instanceof KeyEvent) {
                            routeKeyEvent((KeyEvent) ev, elID);
                        } else {
                            System.out.println("Unsupported Event: " + ev);
                        }
                    });
        } else { //No ID - find it's container
            if (evSrc instanceof Hyperlink) {
                routeHrefEvt((MouseEvent) ev);
            }
        }
    }

    /**
     * Route a MouseEvent to the correct location.
     *
     * @param mev Event.
     * @param el Scriptable Element.
     */
    private void routeMouseEvent(final MouseEvent mev, final Scriptable el) {
        final var evType = mev.getEventType();
        try {
            if (evType == MouseEvent.MOUSE_PRESSED || evType == MouseEvent.MOUSE_RELEASED || evType == MouseEvent.MOUSE_CLICKED) {
                var down = (mev.getEventType() == MouseEvent.MOUSE_PRESSED); //Is the mouse pressed right now?
                scriptingEngine.invokeOnElement(el, Scripting.CLICK_FN, mev.getButton(), mev.getX(), mev.getY(), down);
            } else if (evType == MouseEvent.MOUSE_MOVED) {
                scriptingEngine.invokeOnElement(el, Scripting.MOUSE_MOVED_FN, mev.getX(), mev.getY());
            } else if (evType == MouseEvent.MOUSE_ENTERED) {
                scriptingEngine.invokeOnElement(el, Scripting.MOUSE_ENTER_FN, mev.getX(), mev.getY());
            } else if (evType == MouseEvent.MOUSE_EXITED) {
                scriptingEngine.invokeOnElement(el, Scripting.MOUSE_EXIT_FN, mev.getX(), mev.getY());
            } else if (evType == MouseEvent.MOUSE_DRAGGED) {
                scriptingEngine.invokeOnElement(el, Scripting.DRAG_FUNCTION, mev.getX(), mev.getY());
            }
        } catch (ScriptException ex) {
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Route a KeyEvent to the correct location.
     *
     * @param kev Event.
     * @param elID Element.
     */
    private void routeKeyEvent(final KeyEvent kev, final String elID) {
        final var evType = kev.getEventType();
        var id = elID;
        if (evType == KeyEvent.KEY_PRESSED || evType == KeyEvent.KEY_RELEASED || evType == KeyEvent.KEY_TYPED) {
            //Key has been pressed
            var elOpt = currentDoc.getElementByID(id);
            final var keyName = kev.getCode().getName();
            System.out.println(kev);
            System.out.println(elID);
            final Boolean down = (evType == KeyEvent.KEY_PRESSED);
            elOpt.ifPresent(el -> {
                try {
                    scriptingEngine.invokeOnElement(el, Scripting.KEY_PRESS_FN, keyName, kev.isControlDown(), kev.isAltDown(), kev.isMetaDown(), down);
                } catch (ScriptException ex) {
                } catch (IOException ex) {
                    System.err.println(ex);
                }
            });
        }
    }

    /**
     * Route an event for an hyperlink.
     *
     * @param ev Mouse event on an hyperlink.
     */
    private void routeHrefEvt(final MouseEvent ev) {
        var evSrc = (javafx.scene.Node) ev.getSource();
        var parEl = evSrc.getParent();
        var parID = parEl.getId();
        while ((parID == null) && (parEl != null)) {
            parEl = parEl.getParent();
            parID = parEl.getId();
        }
        if (parEl == null) { //Failed to find a valid parent
            putMessage("Warning: Couldn't route href evt: " + ev, false);
            return;
        }
        var elOpt = currentDoc.getElementByID(parID); //Get the parent element
        elOpt.ifPresent(el -> {
            if (el instanceof ShapeElement) {
                var segs = ((ShapeElement) el).getText();
                for (StyledTextSeg seg : segs.get()) {
                    if (seg.isHref()) {
                        var segStr = seg.getString();
                        var hlStr = ((Hyperlink) evSrc).getText();
                        if (segStr.equals(hlStr)) {
                            handleHrefEvt(seg, ev);
                        }
                    }
                }
            } else {
                putMessage("Warning: Bad href parent type" + el.getRealType(), false);
            }
        });
    }

    /**
     * Handle an event on an hyperlink.
     *
     * @param hrefSeg Href.
     * @param mev Event.
     */
    private void handleHrefEvt(final StyledTextSeg hrefSeg, final MouseEvent mev) {
        System.out.println("We got it: " + hrefSeg);
        if (hrefSeg.getRefType() == StyledTextSeg.REF_TYPE.EXTERNAL) {
            if (mev.getEventType() == MouseEvent.MOUSE_CLICKED) {
                var os = System.getProperty("os.name").toLowerCase();
                try {
                    if (os.contains("win")) { //Windows apparently works like this:
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + hrefSeg.getRefTarget());
                    } else if (os.contains("mac")) {
                        //MacOS has the open command
                        Runtime.getRuntime().exec("open " + hrefSeg.getRefTarget());
                    } else if (os.contains("lin")) { //Most Linuxen have xdg-open
                        Runtime.getRuntime().exec("xdg-open " + hrefSeg.getRefTarget());
                    } else if (os.contains("nix")) { //Might be a BSD - firefox is the best bet.
                        Runtime.getRuntime().exec("firefox " + hrefSeg.getRefTarget());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else { //Must be an internal ref.
            var maybeTargetEl = currentDoc.getElementByID(hrefSeg.getRefTarget());
            maybeTargetEl.flatMap(e -> e.getPage()).ifPresent(p -> gotoPage(p.getID(), true));
        }

    }

    /**
     * Handle an event from a button.
     *
     * @param ev Button event.
     */
    private void handleButtonEvent(final Event ev) {
        if (ev instanceof ActionEvent) {
            var aev = (ActionEvent) ev;
            var source = (Button) aev.getSource();
            if (source.getId().contains("-jump-card-button")) {
                handleNavButtonEvent(aev, source);
            }
        }
    }

    /**
     * Handle an event from a Navigation button.
     *
     * @param aev Button event.
     * @param target Target button.
     */
    private void handleNavButtonEvent(final ActionEvent aev,
            final Button target) {
        if (((Button) target).getId().contains("-jump-card-button")) {
            var id = ((Button) target).getId().replace("-jump-card-button", "");
            this.gotoPage(id, true);
        }
    }

    /**
     * Parse a new document archive.
     *
     * @param xmlFile Doc to load
     */
    private void parseNewDoc(final File xmlFile) { // Load a new doc
        initDoc(new DocIO(xmlFile.getAbsolutePath()));
    }

    /**
     * Parse an internal document archive from stream.
     *
     * @param archStream Doc to load
     */
    private void parseNewDoc(final InputStream archStream) {
        initDoc(new DocIO(archStream));
        //Platform.runLater(() -> controller.showPlayable("test-player", new SizeObj(200d, 200d, 0d), new LocObj(new Point2D(50d, 50d), 0d), "file:/home/david/Videos/Popcornarchive-aClockworkOrange1971.mp4"));
    }

    /**
     * Initialise/load doc.
     *
     * @param docio doc to init.
     */
    private void initDoc(final DocIO docio) {
        if (docIO != null) {
            docIO.close(); //Close the previous
        }
        docIO = docio;
        eventQueue.clear();
        callQueue.clear();
        docQueue.clear();
        var parsed = docIO.getDoc();
        if (parsed.isEmpty()) {
            // Oops, couldn't parse doc
            putMessage("Doc parse error", true);
            return;
        }
        var doc = parsed.get();
        Platform.runLater(
                () -> {
                    controller.clearCardButtons();
                    controller.clearCard("");
                    controller.setCursorType(javafx.scene.Cursor.DEFAULT);
                });

        var child = doc.getRootElement();
        if (child instanceof DocElement) { //Make sure that doc is sane.
            currentDoc = (DocElement) child;
            currentDoc.setTopLevelBindings(scriptingEngine.getTopLevelBindings()); //Attach the globals.
            scriptingEngine.setGlobal("doc", currentDoc); //Expose the doc to the scripting engine.
            //Check for and print any validation errors. Don't really care though.
            var valErrs = currentDoc.getValidationErrors();
            if (valErrs.size() > 0) {
                var errStr = String.join("\n", valErrs);
                System.err.println("Validation Errors Found:\n" + errStr);
            }
            //When the doc changes, redraw the element that has changed.
            currentDoc.setChangeCallback(
                    el -> this.redrawEl(el)
            );
            //Add buttons for each page/card
            var it = currentDoc.getPages().listIterator();
            while (it.hasNext()) {
                var ind = it.nextIndex();
                var page = it.next();
                Platform.runLater(
                        () -> {
                            var tiopt = page.getTitle();
                            String id = page.getID();
                            String title = tiopt.isPresent() ? tiopt.get() : id;
                            controller.addCardButton(title, id, ind);
                        });
            }

            try {
                //Init Document global scripts by running onLoad function.
                scriptingEngine.invokeOnElement(currentDoc, Scripting.LOAD_FUNCTION);
            } catch (ScriptException | IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Initialise first page
            this.gotoPage(0, true);
        } else {
            putMessage("Malformed Doc - not Doc Element!", true);
            // Looks like doc is malformed
        }
        System.out.println("New document loaded");
    }

    /**
     * Instruct the UI to draw an image.
     *
     * @param img Image to draw.
     */
    public void drawImageEl(final ImageElement img) {
        /*
        Enforce thread boundary!
         */
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> drawImageEl(img));
            return;
        }
        var sourceOpt = img.getSourceLoc();
        var id = img.getID();

        var source = (sourceOpt.isPresent()) ? sourceOpt.get() : "";
        Platform.runLater(() -> controller.drawImage(id, source, false));
    }

    /**
     * Instruct the UI to draw an image using discrete values.
     *
     * @param id Image ID
     * @param xSize Image X Size.
     * @param ySize Image Y Size.
     * @param xLoc X Location.
     * @param yLoc Y Location.
     * @param zInd Z Index.
     * @param source Image Source.
     */
    //CHECKSTYLE:OFF
    public void putImage(final String id, final Double xSize, final Double ySize, final Double rot, final Double xLoc,
            final Double yLoc, final Double zInd, final String source) {
        //CHECKSTYLE:ON
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> putImage(id, xSize, ySize, rot, xLoc, yLoc, zInd, source));
            return;
        }
        Platform.runLater(() -> {
            var loc = new LocObj(new Point2D(xLoc, yLoc), zInd);
            var size = new SizeObj(xSize, ySize, rot);
            controller.drawImage(id, source, false);
            controller.moveElement(id, loc);
            controller.resizeElement(id, size);
        });
    }

    /**
     * Instruct the UI to draw a shape.
     *
     * @param shape Shape to draw.
     */
    private void drawShapeEl(final ShapeElement shape) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> drawShapeEl(shape));
            return;
        }
        ArrayList<StyledTextSeg> textSegs;
        StrokeProps stroke;
        var shapeType = shape.getType();
        final var strokeOpt = shape.getStroke();
        final var id = shape.getID();

        var textOpt = shape.getText();
        if (textOpt.isPresent()) {
            textSegs = textOpt.get();
        } else {
            textSegs = new ArrayList<>();
        }

        if (strokeOpt.isPresent()) {
            stroke = strokeOpt.get();
        } else {
            stroke = new StrokeProps();
        }

        Platform.runLater(
                () -> {
                    controller.updateShape(
                            id,
                            shapeType,
                            stroke,
                            textSegs,
                            shape.getSegPoints());
                });
    }

    /**
     * Put a shape to the display.
     *
     * @param id Shape ID.
     * @param type Shape type.
     */
    public void putShape(final String id, final String type) {
        Platform.runLater(
                () -> {
                    controller.updateShape(
                            id,
                            type,
                            new StrokeProps(),
                            new ArrayList<>(),
                            new ArrayList<>());

                });
    }

    /**
     * Set a Shape's style.
     *
     * @param id Target shape ID.
     * @param colour Fill colour.
     */
    public void setShapeColour(final String id, final String colour) {
        final var props = new VisualProps();
        props.put(VisualProps.FILL, Color.valueOf(colour));
        Platform.runLater(() -> controller.updateShapeColour(id, Color.valueOf(colour)));
    }

    /**
     * Set text on a shape. Sets properties for all text in the shape.
     *
     * @TODO for linting - reduce the amount of parameters called to 7 if
     * possible
     * @param shapeID Target ID.
     * @param text Text to set.
     * @param hAlign Horizontal alignment.
     * @param vAlign Vertical alignment.
     * @param font Font name.
     * @param colour Font colour.
     * @param size Font size.
     * @param underscore Under-line text.
     * @param italic Italicise text.
     * @param bold Bold text.
     */
    public void setShapeText(final String shapeID, final String text, final String hAlign, final String vAlign, final String font, final String colour,
            final Double size, final Boolean underscore, final Boolean italic, final Boolean bold) {
        var props = new FontProps();
        props.put(FontProps.ALIGNMENT, hAlign);
        props.put(FontProps.VALIGNMENT, vAlign);
        props.put(FontProps.COLOUR, Color.valueOf(colour));
        props.put(FontProps.FONT, font);
        props.put(FontProps.SIZE, size);
        props.put(FontProps.US, underscore);
        props.put(FontProps.IT, italic);
        props.put(FontProps.BOLD, bold);

        var segArr = new ArrayList<StyledTextSeg>();
        var seg = new StyledTextSeg(props, text);
        segArr.add(seg);

        Platform.runLater(() -> {
            controller.updateShapeText(shapeID, segArr);
        });
    }

    /**
     * Set a shape's stroke.
     *
     * @param shapeID Target Shape ID.
     * @param colour Colour to set.
     * @param style Stroke Style.
     * @param width Stroke Width.
     */
    public void setShapeStroke(final String shapeID, final String colour, final String style, final Double width) {
        final StrokeProps props = new StrokeProps();
        props.put(StrokeProps.COLOUR, Color.valueOf(colour));
        props.put(StrokeProps.LINE_STYLE, style);
        props.put(StrokeProps.WIDTH, width);
        Platform.runLater(() -> {
            controller.updateShapeStroke(shapeID, props);
        });
    }

    /**
     * Resize an element on screen.
     *
     * @param id Element ID.
     * @param x New X size.
     * @param y New Y size.
     * @param rot New rotation (degrees).
     */
    public void resizeElement(final String id, final Double x, final Double y, final Double rot) {
        Platform.runLater(() -> {
            controller.resizeElement(id, new SizeObj(x, y, rot));
        });
    }

    /**
     * Set a drawn element's on-screen visibility. Does not modify data.
     *
     * @param id Element ID.
     * @param vis Visibility.
     */
    public void setElementVisibility(final String id, final Boolean vis) {
        Platform.runLater(() -> controller.setElementVisible(id, vis));
    }

    /**
     * Remove an element from the screen. Does not modify data.
     *
     * @param id Element.
     */
    public void removeElementFromScreen(final String id) {
        Platform.runLater(() -> controller.remove(id));
    }

    /**
     * Move an element.
     *
     * @param id Element ID.
     * @param x New X location.
     * @param y New Y location.
     * @param zIndex New Z Index
     */
    public void moveElement(final String id, final Double x, final Double y, final Double zIndex) {
        Platform.runLater(() -> {
            controller.moveElement(id, new LocObj(new Point2D(x, y), zIndex));
        });
    }

    /**
     * Set basic shadow on an element.
     *
     * @param id Target ID.
     * @param radius Shadow radius.
     */
    public void setElementShadow(final String id, final Double radius) {
        Platform.runLater(() -> {
            controller.setElShadow(id, radius);
        });
    }

    /**
     * Show a player.
     *
     * @param id Player ID.
     * @param mediaLoc Media location.
     * @param showControls Display controls?
     * @param autoPlay Auto-play media?
     * @param loop Loop media?
     * @param offset Media offset (in seconds).
     */
    public void drawPlayer(final String id, final String mediaLoc, final Boolean showControls, final Boolean autoPlay, final Boolean loop, final Double offset) {
        Platform.runLater(() -> {
            controller.showPlayable(id, mediaLoc, showControls, loop, autoPlay, offset);
        });
    }

    /**
     * Make a player on screen play/pause.
     *
     * @param id Player ID.
     * @param play Play/Pause.
     */
    public void togglePlayerPlaying(final String id, final Boolean play) {
        Platform.runLater(() -> controller.togglePlayerPlaying(id, play));
    }

    /**
     * Instruct the UI to create a player for the element.
     *
     * @param playable Playable element.
     */
    private void drawPlayableEl(final PlayableElement playable) {
        /*
        Enforce thread boundary!
         */
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> drawPlayableEl(playable));
            return;
        }
        var sourceOpt = playable.getSourceLoc();
        var source = (sourceOpt.isPresent()) ? sourceOpt.get() : "";
        var id = playable.getID();
        drawPlayer(id, source, playable.getDisplayPlayer(), playable.getAutoplay(), playable.getLoop(), playable.getSeekOffset());
    }

    public void deleteElement(final String id) {
        Optional<VisualElement> maybeEl = currentDoc.getElementByID(id);
        //maybeEl.ifPresent(e -> e.);
    }

    /**
     * Set the cursor type.
     *
     * @param cType String of Cursor enum value.
     */
    public void setCursorType(final String cType) {
        Platform.runLater(() -> controller.setCursorType(javafx.scene.Cursor.cursor(cType.toUpperCase())));
    }

    /**
     * Go to next sequential page.
     */
    public void gotoNextPage() {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> gotoNextPage());
            return;
        }
        currentDoc.getCurrentPage().map(card -> {
            if (card.getIndex() < currentDoc.getPages().size() - 1) {
                return (Integer) (card.getIndex() + 1);
            }
            return null;
        }).ifPresent(next -> this.gotoPage((Integer) next, true));
    }

    /**
     * Go to last visited page.
     */
    public void gotoPrevPage() {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> gotoPrevPage());
            return;
        }
        if (!navHistory.isEmpty()) {
            this.gotoPage(navHistory.pop(), false);
        }
    }

    /**
     * Go to specified page number.
     *
     * @param pageNum Number to go to.
     * @param storeHistory Should I record it in history?
     */
    public void gotoPage(final Integer pageNum, final Boolean storeHistory) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> gotoPage(pageNum, storeHistory));
            return;
        }
        if (storeHistory) {
            // Push previous to stack
            currentDoc.getCurrentPage().ifPresent(p -> navHistory.push(p.getID()));
        }
        currentDoc.getPage(pageNum)
                .ifPresent(f -> gotoPage(f, storeHistory));
    }

    /**
     * Go to specified page.
     *
     * @param pageID ID to go to.
     * @param storeHistory Should I record it in history?
     */
    public void gotoPage(final String pageID, final Boolean storeHistory) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> gotoPage(pageID, storeHistory));
            return;
        }
        if (storeHistory) {
            // Push previous to stack
            currentDoc.getCurrentPage().ifPresent(p -> navHistory.push(p.getID()));
        }
        Optional<PageElement> page = currentDoc.getPage(pageID);
        page.ifPresent(f -> gotoPage(f, storeHistory));
    }

    /**
     * Go to provided page.
     *
     * @param page Page to go to.
     * @param storeHistory Should I record it in history?
     */
    public void gotoPage(final PageElement page, final Boolean storeHistory) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> gotoPage(page, storeHistory));
            return;
        }
        processEls(page);
        putMessage("Loaded New Card: " + currentDoc.getCurrentPage().get().getID(), false);
    }

    /**
     * Configure the card.
     *
     * @param page card to configure display for.
     */
    public void configCard(final PageElement page) {
        Platform.runLater(
                () -> {
                    controller.clearCard("");
                    controller.configCard(page.getSize(),
                            page.getFillColour(), page.getID());
                });
    }

    /**
     * Process elements on a page.
     *
     * @param el Element
     */
    public void processEls(final VisualElement el) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> processEls(el));
            return;
        }
//CHECKSTYLE:OFF
        //Check if there should be a delay.
        var maybeDelay = el.getDelaySecs();
        maybeDelay.ifPresentOrElse(del -> { //Element should be delayed
            Double delaymS = del * 1000;
            Runnable delayTask = () -> {
                redrawEl(el);
                // Then recurse the children
                for (int i = 0; i < el.getChildCount(); i++) {
                    var ch = el.getChild(i);
                    if (ch instanceof VisualElement) {
                        processEls((VisualElement) ((Element) ch));
                    }
                }
            };
            elementDelayService.schedule(delayTask, delaymS.longValue(), TimeUnit.MILLISECONDS);
        }, () -> { //No delay.
            // Do whatever you're going to do with this node…
            redrawEl(el);
            // Then recurse the children
            for (int i = 0; i < el.getChildCount(); i++) {
                var ch = el.getChild(i);
                if (ch instanceof VisualElement) {
                    processEls((VisualElement) ((Element) ch));
                }
            }
        });
//CHECKSTYLE:ON
    }

    /**
     * Re/draws a Visual element.
     *
     * @param el Visual Element.
     */
    public void redrawEl(final VisualElement el) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> redrawEl(el));
            return;
        }
        //Check that the element should still be on screen.
        var pg = el.getPage().get();
        if (!pg.equals(currentDoc.getCurrentPage().get())) {
            return;
        }
        if (el instanceof PageElement) {
            this.configCard((PageElement) el);
        } else if (el instanceof ImageElement) {
            this.drawImageEl((ImageElement) el);
        } else if (el instanceof ShapeElement) {
            this.drawShapeEl((ShapeElement) el);
        } else if (el instanceof PlayableElement) {
            this.drawPlayableEl((PlayableElement) el);
        }
        var propsMap = el.getVisualProps();
        var id = el.getID();
        var maybeSize = el.getSize();
        var maybeLoc = el.getOrigin();

        Platform.runLater(() -> {
            controller.setElVisualProps(id, propsMap);
            maybeSize.ifPresent(s -> controller.resizeElement(id, s));
            maybeLoc.ifPresent(l -> controller.moveElement(id, l));
        });
        //If element is scriptable, evaluate it's load-function.
        if (el instanceof Scriptable) {
            try {
                scriptingEngine.invokeOnElement(el, Scripting.LOAD_FUNCTION);
            } catch (ScriptException | IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //CHECKSTYLE:OFF
        //Check if there should be a finite duration.
        var maybeDuration = el.getDurationSecs();
        maybeDuration.ifPresent(dur -> {
            Double durmS = dur * 1000;
            Runnable delayTask = () -> {
                if (el instanceof PageElement) {
                    gotoNextPage(); //Load next page if I'm a page.
                } else {
                    setElementVisibility(el.getID(), false); //Else just hide the element.
                }
            };
            elementDelayService.schedule(delayTask, durmS.longValue(), TimeUnit.MILLISECONDS);
        });
        //CHECKSTYLE:ON
    }

    /**
     * Evaluate a python string.
     *
     * @param code Code string.
     */
    public void evalPyStr(final String code) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> evalPyStr(code));
            return;
        }
        try {
            scriptingEngine.evalString(code, "python");
        } catch (ScriptException ex) {
            putMessage(ex.getMessage(), true);
        }
    }

    /**
     * Get the string of an Element's script. Returns an empty string if none
     * available.
     *
     * @param id Element ID.
     * @return Script string.
     */
    public String getElScript(final String id) {
        var maybeEl = currentDoc.getElementByID(id);
        var maybeScr = maybeEl.flatMap(e -> e.getScriptEl())
                .flatMap(s -> s.getSourceLoc())
                .flatMap(sl -> docIO.getResource(sl))
                .map(b -> new String(b, StandardCharsets.UTF_8));
        if (maybeScr.isPresent()) {
            return maybeScr.get();
        } else {
            return "";
        }
    }

    /**
     * Get an element's language.
     *
     * @param id Element ID.
     * @return Language. python if not present.
     */
    public String getElScriptLang(final String id) {
        var maybeEl = currentDoc.getElementByID(id);
        var maybeLang = maybeEl.flatMap(e -> e.getScriptEl())
                .map(s -> s.getScriptLang());
        if (maybeLang.isPresent()) {
            return maybeLang.get();
        } else {
            return "python";
        }
    }

    /**
     * Set an Element's script.
     *
     * @param id Element ID.
     * @param lang Language.
     * @param scr Script.
     */
    public void setElScript(final String id, final String lang, final String scr) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> setElScript(id, lang, scr));
            return;
        }
        var maybeEl = currentDoc.getElementByID(id);
        maybeEl.flatMap(e -> e.getScriptEl()).flatMap(sel -> sel.getSourceLoc()).ifPresent(loc -> docIO.removeResource(loc)); //Remove existing
        maybeEl.ifPresent(e -> {
            String suffix = "";
            if (lang.toLowerCase().equals("rhino")) {
                suffix = ".js";
            } else if (lang.toLowerCase().equals("python")) {
                suffix = ".py";
            }
            try {
                Path file = docIO.getEmptyFile("/scripts", id, suffix);
                docIO.writeBytes(file.toString(), scr.getBytes());
                e.addScriptFile(file, lang);
            } catch (IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    /**
     * Get the current Tool IO.
     *
     * @return toolIO.
     */
    public ToolIO getToolIO() {
        return toolIO;
    }

    /**
     * Load Tools from XML.
     *
     * @return Optional<Tools>
     */
    private Optional<Tools> loadTools() {
        var toolsStream = Engine.class.getResourceAsStream("tools.zip");
        if (toolsStream == null) {
            this.putMessage("Unable to load Tools", true);
            return Optional.empty();
        }
        toolIO = new ToolIO(toolsStream);
        var parsedDoc = toolIO.getDoc();
        var root = parsedDoc
                .filter(d -> d.getRootElement() instanceof Tools)
                .map(d -> (Tools) d.getRootElement());
        //this.putMessage("Tools Loaded", false);
        return root;
    }

    /**
     * Activate tool
     *
     * @param toolID Tool to activate.
     */
    public void activateTool(final String toolID) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> activateTool(toolID));
            return;
        }
        var maybeCurrentTool = currentTools.getTool(currentToolID);
        //Invoke the close/cleanup function
        maybeCurrentTool.ifPresent(t -> {
            try {
                scriptingEngine.invokeOnElement(t, Scripting.TOOL_CLOSE_FUNCTION);
            } catch (ScriptException | IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        currentToolID = toolID; //Set current tool
        var maybeTool = currentTools.getTool(toolID);
        maybeTool.ifPresent(t -> {
            try {
                scriptingEngine.invokeOnElement(t, Scripting.LOAD_FUNCTION);
            } catch (ScriptException | IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
            controller.toggleBubble(t.bubbleEvents());
        });
    }

    /**
     * Route incoming event to tools.
     *
     * @param ev Event.
     * @return Incoming Event if tool is not a Sink.
     */
    private Optional<Event> routeToolEvent(final Event ev) {
        //This is all kind of yucky, but nvm.
        var maybeTool = currentTools.getTool(currentToolID);
        Optional<Event> ret = Optional.of(ev);
        if (ev.getSource().getClass().equals(Pane.class)) {
            if (ev instanceof MouseEvent) { //Only giving tools mousevents right now.
                maybeTool.ifPresent(t -> routeMouseEvent((MouseEvent) ev, t));
            }
        }
        if ((maybeTool.isPresent()) && (ev instanceof MouseEvent)) {
            var tool = maybeTool.get();
            ret = (tool.sinkEvents() == true) ? Optional.empty() : ret; //Get whether to sink the event.
        }
        return ret;
    }

    /**
     * The UI has relocated an element. It upsets me that I can't just easily
     * use Events, but hey ho.
     *
     * @param elID Element ID.
     * @param newLoc New location.
     */
    public void elementRelocated(final String elID, final LocObj newLoc) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> elementRelocated(elID, newLoc));
            return;
        }
        var elOpt = currentDoc.getElementByID(elID);
        elOpt.ifPresent(el -> {
            el.setOriginXY(newLoc);
            el.hasUpdated();
        });
    }

    /**
     * Provide the Element's properties to the UI.
     *
     * @param elID Element ID.
     */
    public void provideProperties(final String elID) {
        Optional<VisualElement> maybeEl = currentDoc.getElementByID(elID);
        maybeEl.ifPresent(el -> {
//            el.
        });
    }

    /**
     * Instruct the UI to show a message to the User.
     *
     * @param message Message to show
     * @param blocking Should I block the User?
     */
    public void putMessage(final String message, final Boolean blocking) {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> putMessage(message, blocking));
            return;
        }
        System.out.println("Message: " + message);
        if (blocking) {
            Platform.runLater(() -> controller.showBlockingMessage(message));
        } else {
            Platform.runLater(() -> controller.showNonBlockingMessage(message));
        }
    }

    /**
     * Receive line from the console.
     *
     * @param line Entered line.
     */
    public void consoleLineCallback(final String line) {
        //Don't execute on this thread
        if (Thread.currentThread() == getThread()) {
            return;
        }

        if (!consoleOnUserInput.get()) {
            runFunction(() -> evalPyStr(line));
        } else {
            consoleLine = line;
            consoleLineReady.countDown();
        }
    }

    /**
     * Read a line from the console. Will wait until data available.
     *
     * @return Line.
     */
    public String readConsoleLine() {
        consoleLineReady = new CountDownLatch(1);
        consoleOnUserInput.set(true);
        Platform.runLater(() -> controller.showConsole());
        try {
            consoleLineReady.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
        consoleOnUserInput.set(false);
        return new String(consoleLine);
    }

    /**
     * Toggle if the display is editable.
     *
     * @param editable Is it editable?
     */
    public void toggleEdit(final Boolean editable) {
        Platform.runLater(() -> controller.toggleEditable(editable));
    }

    /**
     * Request the UI to show a doc chooser.
     */
    public void showDocChooser() {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> showDocChooser());
            return;
        }
        Platform.runLater(() -> controller.showDocPicker());
    }

    /**
     * Attempt to save the current doc.
     */
    public void saveCurrentDoc() {
        if (docIO.canSave() == false) {
            Platform.runLater(() -> controller.showSavePicker());
            return;
        }
        try {
            docIO.save();
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            putMessage("Saving Failed: " + ex, true);
            return;
        }
        putMessage("Saved!", false);
    }

    /**
     * Attempt to save current doc to new location.
     *
     * @param newPath new location to save to.
     */
    public void saveCurrentDocAs(final String newPath) {
        try {
            docIO.saveAs(newPath);
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            putMessage("Saving Failed: " + ex, true);
            return;
        }
        putMessage("Saved!", false);
    }

    /**
     * Loads the start screen.
     */
    public void showStartScreen() {
        if (Thread.currentThread() != getThread()) {
            runFunction(() -> showStartScreen());
            return;
        }
        //Load the start screen
        var startXmlStream = MainController.class
                .getResourceAsStream(startScreenFileName);
        parseNewDoc(startXmlStream);
    }

    /**
     * Load an empty document.
     */
    public void loadEmptyDoc() {
        var emptyDocFile = MainController.class
                .getResourceAsStream(emptyFileName);
        parseNewDoc(emptyDocFile);
    }

    /**
     * Exit Application.
     */
    public void exit() {
        Platform.runLater(() -> controller.gracefulExit());
    }
}
