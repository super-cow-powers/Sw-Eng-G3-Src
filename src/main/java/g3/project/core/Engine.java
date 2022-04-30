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
import g3.project.xmlIO.Io;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javax.script.ScriptException;
import nu.xom.Document;
import nu.xom.Element;

/**
 * @author david
 */
public final class Engine extends Threaded {

    /**
     * XML IO.
     */
    private Io docIO;

    /**
     * Tools IO.
     */
    private Io toolIO;

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
    private ArrayList<Tool> myTools;
    /**
     * Document currently open.
     */
    private DocElement currentDoc;
    /**
     * Pages in current doc.
     */
    private final ArrayList<PageElement> currentPages = new ArrayList<>();
    /**
     * ID of currently open page/card.
     */
    private String currentPageID = "";
    /**
     * Navigation history stack.
     */
    private final Stack<String> navHistory = new Stack<>();

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
     * Constructor.
     *
     * @param uiController Ref to the main UI controller.
     */
    public Engine(final MainController uiController) {
        super();
        this.controller = uiController;
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
    public Io getDocIO() {
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
            scriptingEngine = new Scripting("python", this);
            scriptingEngine.setGlobal("pages", currentPages);
            //Show Start Screen
            showStartScreen();
            // Load in the tools
            loadTools()
                    .ifPresentOrElse(
                            t -> myTools = t.getTools(),
                            () -> {
                                myTools = new ArrayList<Tool>();
                                Platform.runLater(() -> controller.
                                showNonBlockingMessage("Failed Loading"
                                        + " Tools!"));
                            });
            // Add tool buttons
            var iterTool = myTools.iterator();
            while (iterTool.hasNext()) {
                var currentTool = iterTool.next();
                Platform.runLater(() -> controller.
                        addTool(currentTool.getName(), currentTool.getID()));
            }
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
                    callQueue.take().run();
                } else { //Nothing to do. Suspend
                    suspended.set(true);
                }

                while (suspended.get()) { // Suspend
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (Exception ex) {
                //Something went wrong.
                ex.printStackTrace();
            }
        }
        netComms.stop();
        System.out.println("Engine is going down NOW.");
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
            routeElementEvent(event);
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
            var elOpt = currentDoc.getElementByID(elID);
            if (ev instanceof MouseEvent) {
                routeMouseEvent((MouseEvent) ev, elID);
            } else if (ev instanceof KeyEvent) {
                routeKeyEvent((KeyEvent) ev, elID);
            } else {
                System.out.println("Unsupported Event: " + ev);
            }
        } else { //No ID - find it's container
            if (evSrc instanceof Hyperlink) {
                routeHrefEvt((MouseEvent) ev);
            }
        }
    }

    private void routeMouseEvent(final MouseEvent mev, final String elID) {
        final var evType = mev.getEventType();
        var elOpt = currentDoc.getElementByID(elID);
        if (evType == MouseEvent.MOUSE_PRESSED || evType == MouseEvent.MOUSE_RELEASED || evType == MouseEvent.MOUSE_CLICKED) {
            var down = (mev.getEventType() == MouseEvent.MOUSE_PRESSED); //Is the mouse pressed right now?
            elOpt.ifPresent(el -> scriptingEngine.invokeOnElement(el, Scripting.CLICK_FN, mev.getButton(), mev.getX(), mev.getY(), down));
        } else if (evType == MouseEvent.MOUSE_MOVED) {
            elOpt.ifPresent(el -> scriptingEngine.invokeOnElement(el, Scripting.MOUSE_MOVED_FN, mev.getX(), mev.getY()));
        } else if (evType == MouseEvent.MOUSE_ENTERED) {
            elOpt.ifPresent(el -> scriptingEngine.invokeOnElement(el, Scripting.MOUSE_ENTER_FN, mev.getX(), mev.getY()));
        } else if (evType == MouseEvent.MOUSE_EXITED) {
            elOpt.ifPresent(el -> scriptingEngine.invokeOnElement(el, Scripting.MOUSE_EXIT_FN, mev.getX(), mev.getY()));
        }
    }

    private void routeKeyEvent(final KeyEvent kev, String elID) {
        final var evType = kev.getEventType();
        if (evType == KeyEvent.KEY_PRESSED || evType == KeyEvent.KEY_RELEASED || evType == KeyEvent.KEY_TYPED) {
            //Key has been pressed
            if (elID.equals("pageScroll")) { //Press routed to page
                elID = currentPageID;
            }
            var elOpt = currentDoc.getElementByID(elID);
            final var keyName = kev.getCode().getName();
            System.out.println(kev);
            final Boolean down = (evType == KeyEvent.KEY_PRESSED);
            elOpt.ifPresent(el -> scriptingEngine.invokeOnElement(el, Scripting.KEY_PRESS_FN, keyName, kev.isControlDown(), kev.isAltDown(), kev.isMetaDown(), down));
        }
    }

    /**
     * Route an event for an hyperlink.
     *
     * @param ev
     */
    private void routeHrefEvt(MouseEvent ev) {
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
                            System.out.println("We got it: " + seg);
                        }
                    }
                }
            } else {
                putMessage("Warning: Bad href parent type" + el.getRealType(), false);
            }
        });
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
     * Parse a new XML document.
     *
     * @param xmlFile Doc to parse
     */
    private void parseNewDoc(final File xmlFile) { // Load a new doc
        docIO = new Io(xmlFile);
        var parsed = docIO.getDoc();
        if (parsed.isPresent()) {
            initDoc(parsed.get());
        } else {
            putMessage("Doc parse error", true);
            // Oops, couldn't parse initial doc.
        }
    }

    /**
     * Parse an (currently internal) XML document from stream.
     *
     * @param docStream Doc to parse
     */
    private void parseNewDoc(final InputStream docStream) {
        docIO = new Io(docStream, "internal_ui");
        var parsed = docIO.getDoc();
        if (parsed.isPresent()) {
            initDoc(parsed.get());
        } else {
            putMessage("Doc parse error", true);
            // Oops, couldn't parse initial doc.
        }
    }

    /**
     * Initialise/load doc.
     *
     * @param doc doc to init.
     */
    private void initDoc(final Document doc) {
        Platform.runLater(
                () -> {
                    controller.clearCardButtons();
                    controller.clearCard("");
                    controller.setViewScale(1d);
                });

        var child = doc.getRootElement();
        if (child instanceof DocElement) { //Make sure that doc is sane.
            currentDoc = (DocElement) child;
            scriptingEngine.setGlobal("document", currentDoc); //Expose the doc to the scripting engine.
            var valErrs = currentDoc.getValidationErrors();
            for (var err : valErrs) {
                System.out.println(err);
            }
            //When the doc changes, redraw the element that has changed.
            currentDoc.setChangeCallback(
                    el -> this.redrawEl(el));
            //Get all pages/cards
            currentDoc
                    .getPages()
                    .ifPresent(
                            f -> {
                                currentPages.clear();
                                currentPages.addAll(f);
                            });
            //Add buttons for each page/card
            var it = currentPages.listIterator();
            while (it.hasNext()) {
                var ind = it.nextIndex();
                var page = it.next();
                Platform.runLater(
                        () -> {
                            var tiopt = page.getTitle();
                            var id = page.getID();
                            var title = tiopt.isPresent() ? tiopt.get() : id;
                            controller.addCardButton(title, id, ind);
                        });
            }

            // Initialise ID for first page
            currentPageID = currentPages.get(0).getID();
            this.gotoPage(currentPageID, true);
            try {
                //Init Document global scripts
                scriptingEngine.evalElement(currentDoc);
            } catch (ScriptException | IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }

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
    public void drawImage(final ImageElement img) {
        /*
        Enforce thread boundary!
         */
        if (Thread.currentThread() != myThread) {
            runFunction(() -> drawImage(img));
            return;
        }
        final double defImgXY = 20d;
        var sourceOpt = img.getSourceLoc();
        var locOpt = img.getOrigin();
        var sizeOpt = img.getSize();
        var id = img.getID();

        var source = (sourceOpt.isPresent()) ? sourceOpt.get() : "";
        var loc = (locOpt.isPresent())
                ? locOpt.get()
                : new LocObj(new Point2D(0, 0), 0d);
        var locPoint = loc.getLoc();
        var size = (sizeOpt.isPresent())
                ? sizeOpt.get() : new SizeObj(defImgXY, defImgXY, 0d);
        drawImage(id, size.getX(), size.getY(), size.getRot(), locPoint.getX(), locPoint.getY(), loc.getZ(), source);
    }

    /**
     * Instruct the UI to draw an image using discrete values.
     *
     * @param ID
     * @param xSize
     * @param ySize
     * @param xLoc
     * @param yLoc
     * @param zInd
     * @param source
     */
    //CHECKSTYLE:OFF
    public void drawImage(final String id, final Double xSize, final Double ySize, final Double rot, final Double xLoc, final Double yLoc, final Double zInd, final String source) {
        //CHECKSTYLE:ON
        if (Thread.currentThread() != myThread) {
            runFunction(() -> drawImage(id, xSize, ySize, rot, xLoc, yLoc, zInd, source));
            return;
        }
        Platform.runLater(() -> {
            var loc = new LocObj(new Point2D(xLoc, yLoc), zInd);
            var size = new SizeObj(xSize, ySize, rot);

            controller.updateImage(id, size, loc, source);
        });
    }

    /**
     * Instruct the UI to draw a shape.
     *
     * @param shape Shape to draw.
     */
    public void drawShape(final ShapeElement shape) {
        if (Thread.currentThread() != myThread) {
            runFunction(() -> drawShape(shape));
            return;
        }
        ArrayList<StyledTextSeg> textSegs;
        StrokeProps stroke;
        var shapeType = shape.getType();
        var strokeOpt = shape.getStroke();

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
                            shapeType,
                            shape.getProps(),
                            stroke,
                            textSegs);
                });
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
        if (Thread.currentThread() != myThread) {
            runFunction(() -> gotoNextPage());
            return;
        }
        var currentCard = getPageIndex(currentPageID);
        if (currentCard < currentPages.size() - 1) {
            currentCard++;
        }
        this.gotoPage(currentCard, true);
    }

    /**
     * Go to last visited page.
     */
    public void gotoPrevPage() {
        if (Thread.currentThread() != myThread) {
            runFunction(() -> gotoPrevPage());
            return;
        }
        this.gotoPage(navHistory.pop(), false);
    }

    /**
     * Go to specified page number.
     *
     * @param pageNum Number to go to.
     * @param storeHistory Should I record it in history?
     */
    public void gotoPage(final Integer pageNum, final Boolean storeHistory) {
        if (Thread.currentThread() != myThread) {
            runFunction(() -> gotoPage(pageNum, storeHistory));
            return;
        }
        var pages = currentDoc.getPages();
        pages.ifPresent(f -> gotoPage(f.get(pageNum), storeHistory));
    }

    /**
     * Go to specified page.
     *
     * @param pageID ID to go to.
     * @param storeHistory Should I record it in history?
     */
    public void gotoPage(final String pageID, final Boolean storeHistory) {
        if (Thread.currentThread() != myThread) {
            runFunction(() -> gotoPage(pageID, storeHistory));
            return;
        }
        var it = currentPages.iterator();
        while (it.hasNext()) {
            var page = it.next();
            var itID = page.getID();
            if (itID.equals(pageID)) {
                gotoPage(page, storeHistory);
            }
        }
    }

    /**
     * Go to provided page.
     *
     * @param page Page to go to.
     * @param storeHistory Should I record it in history?
     */
    public void gotoPage(final PageElement page, final Boolean storeHistory) {
        if (Thread.currentThread() != myThread) {
            runFunction(() -> gotoPage(page, storeHistory));
            return;
        }
        if (storeHistory) {
            navHistory.push(currentPageID); // Push previous to stack
        }
        processEls(page);
        currentPageID = page.getID();
        putMessage("Loaded New Card: " + currentPageID, false);
    }

    /**
     * Configure the card.
     *
     * @param page card to configure display for.
     */
    public void configCard(final PageElement page) {
        Platform.runLater(
                () -> {
                    controller.clearCard(currentPageID);
                    controller.configCard(page.getSize(),
                            page.getFillColour(), page.getID());
                });
    }

    /**
     * Get the index of specified page.
     *
     * @param pageID page.
     * @return Index, or -1 on failure
     */
    private Integer getPageIndex(final String pageID) {
        var it = currentPages.iterator();
        var i = 0;
        while (it.hasNext()) {
            var page = it.next();
            var itID = page.getID();
            if (itID.equals(pageID)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Process elements on a page.
     *
     * @param el Element
     */
    public void processEls(final VisualElement el) {
        if (Thread.currentThread() != myThread) {
            runFunction(() -> processEls(el));
            return;
        }
        // Do whatever you're going to do with this node…
        redrawEl(el);
        //If element is scriptable, evaluate it.
        if (el instanceof Scriptable) {
            try {
                scriptingEngine.evalElement(el);
            } catch (ScriptException | IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Then recurse the children
        for (int i = 0; i < el.getChildCount(); i++) {
            var ch = el.getChild(i);
            if (ch instanceof VisualElement) {
                processEls((VisualElement) ((Element) ch));
            }
        }
    }

    /**
     * Re/draws a Visual element.
     *
     * @param el Visual Element.
     */
    public void redrawEl(final VisualElement el) {
        if (Thread.currentThread() != myThread) {
            runFunction(() -> redrawEl(el));
            return;
        }
        if (el instanceof PageElement) {
            this.configCard((PageElement) el);
        } else if (el instanceof ImageElement) {
            this.drawImage((ImageElement) el);
        } else if (el instanceof ShapeElement) {
            this.drawShape((ShapeElement) el);
        }
    }

    /**
     * Load Tools from XML.
     *
     * @return Optional<Tools>
     */
    private Optional<Tools> loadTools() {
        var toolsXMLPath = Engine.class.getResource("tools.xml").getPath();
        toolIO = new Io(new File(toolsXMLPath), new ToolsFactory());
        var parsedDoc = toolIO.getDoc();
        var root
                = parsedDoc
                        .filter(d -> d.getRootElement() instanceof Tools)
                        .map(d -> (Tools) d.getRootElement());
        this.putMessage("Tools Loaded", false);
        return root;
    }

    /**
     * Instruct the UI to show a message to the User.
     *
     * @param message Message to show
     * @param blocking Should I block the User?
     */
    public void putMessage(final String message, final Boolean blocking) {
        if (Thread.currentThread() != myThread) {
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
     * Request the UI to show a doc chooser.
     */
    public void showDocChooser() {
        if (Thread.currentThread() != myThread) {
            runFunction(() -> showDocChooser());
            return;
        }
        Platform.runLater(() -> controller.showDocPicker());
    }

    /**
     * Loads the start screen.
     */
    public void showStartScreen() {
        if (Thread.currentThread() != myThread) {
            runFunction(() -> showStartScreen());
            return;
        }
        eventQueue.clear();
        callQueue.clear();
        docQueue.clear();
        //Load the start screen
        var startXmlStream = MainController.class
                .getResourceAsStream("start_screen.xml");
        parseNewDoc(startXmlStream);
    }
}
