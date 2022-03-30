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
import g3.project.ui.LocObj;
import g3.project.ui.MainController;
import g3.project.ui.SizeObj;
import g3.project.xmlIO.Ingestion;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import nu.xom.Element;

/**
 * @author david
 */
public final class Engine implements Runnable {

    /**
     * Thread I'm to run on.
     */
    private Thread engineThread;
    /**
     * XML IO.
     */
    private final Ingestion ingest = new Ingestion();
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
    private ArrayList<PageElement> currentPages;
    /**
     * ID of currently open page/card.
     */
    private String currentPageID = "";
    /**
     * Navigation history stack.
     */
    private final Stack<String> navHistory = new Stack<>();
    /**
     * Factory/manager for all script engines.
     */
    private ScriptEngineManager scriptingEngineManager;
    /**
     * Am I running?
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /**
     * Am I suspended?
     */
    private final AtomicBoolean suspended = new AtomicBoolean(false);
    /**
     * Is the UI available?
     */
    private final AtomicBoolean uiAvailable = new AtomicBoolean(false);

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
     * Ref to the UI controller.
     */
    private final MainController controller;

    /**
     * Constructor.
     *
     * @param uiController Ref to the main UI controller.
     */
    public Engine(final MainController uiController) {
        this.controller = uiController;
    }

    /**
     * Start the Engine.
     */
    public void start() {
        engineThread = new Thread(this);
        engineThread.start();
        running.set(true);
    }

    /**
     * Stop the engine.
     */
    public void stop() {
        running.set(false);
        unsuspend();
    }

    /**
     * Tell the engine that the UI is now active.
     */
    public void allowDraw() {
        uiAvailable.set(true);
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
     * Unsuspend engine if required.
     */
    private synchronized void unsuspend() {
        // Trigger notify if suspended
        if (suspended.get()) {
            suspended.set(false);
            notify();
        }
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        // Init script engine manager
        scriptingEngineManager = new ScriptEngineManager();

        while (!(running.get())) {
        }

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
        // Quit if running flag set to false
        while (running.get()) {
            try {
                if (!docQueue.isEmpty()) { // New doc request?
                    parseNewDoc(docQueue.take());
                } else if (!eventQueue.isEmpty()) { // New event?
                    handleEvent(eventQueue.take());
                } else {
                    suspended.set(true);
                }

                while (suspended.get()) { // Suspend
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("Engine is going down NOW.");
        return;
    }

    /**
     * Handle an incoming event.
     *
     * @param event Event to handle.
     */
    private void handleEvent(final Event event) {
        System.out.println("g3.project.core.Engine.handleEvent()");
        System.out.println(event);
        var evTgt = event.getTarget();
        if (evTgt instanceof Button) {
            handleButtonEvent(event);
        } else if (event instanceof KeyEvent) {
            var kev = (KeyEvent) event;

            if (kev.getCode() == KeyCode.LEFT) {
                gotoPrevPage();
            } else if (kev.getCode() == KeyCode.RIGHT) {
                gotoNextPage();
            }
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
            var target = aev.getTarget();
            if (target instanceof Button) {
                handleNavButtonEvent(aev, (Button) target);
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

        var parsed = ingest.parseDocXML(xmlFile);
        if (parsed.isPresent()) {
            Platform.runLater(
                    () -> {
                        controller.clearCardButtons();
                        controller.clearCard("");
                        controller.setViewScale(1d);
                    });
            var child = parsed.get().getChild(0);
            if (child instanceof DocElement) {
                currentDoc = (DocElement) child;
                currentDoc
                        .GetPages()
                        .ifPresent(
                                f -> {
                                    currentPages = f;
                                });
                // Add buttons for each page
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

            } else {
                putMessage("Malformed Doc - not Doc Element!", true);
                // Looks like doc is malformed
            }
            System.out.println("New document loaded");
        } else {
            putMessage("Doc parse error", true);
            // Oops, couldn't parse initial doc.
        }
    }

    /**
     * Instruct the UI to draw an image.
     *
     * @param img Image to draw.
     */
    public void drawImage(final ImageElement img) {
        var defXY = 20d;
        var sourceOpt = img.getSourceLoc();
        var locOpt = img.getLoc();
        var sizeOpt = img.getSize();
        var id = img.getID();
        Platform.runLater(() -> {
            var source = (sourceOpt.isPresent()) ? sourceOpt.get() : "";
            var loc = (locOpt.isPresent())
                    ? locOpt.get()
                    : new LocObj(new Point2D(0, 0), null, null, 0d);
            var size = (sizeOpt.isPresent())
                    ? sizeOpt.get() : new SizeObj(defXY, defXY, 0d);

            controller.updateImage(id, size, loc, source);
        });
    }

    /**
     * Instruct the UI to draw a shape.
     *
     * @param shape Shape to draw.
     */
    public void drawShape(final ShapeElement shape) {

        ArrayList<FontElement> fontBlocks = new ArrayList<>();
        FontProps fontProps;
        String textString;
        var size = shape.getSize();
        var loc = shape.getLoc();
        var shapeType = shape.getType();
        var fillOpt = shape.getFillColour();
        Color fill;
        if (fillOpt.isPresent()) {
            fill = fillOpt.get();
        } else {
            fill = Color.WHITESMOKE;
        }
        var textOpt = shape.getText();
        if (textOpt.isPresent()) {
            fontBlocks = textOpt.get().getFontBlocks();
        }

        if (fontBlocks.size() > 0) {
            textString = fontBlocks.get(0).getValue();
            fontProps = fontBlocks.get(0).getProperties();
        } else {
            fontProps = null;
            textString = "";
        }
        if (size.isPresent() && loc.isPresent()) {
            Platform.runLater(
                    () -> {
                        if (fontProps != null) {
                            controller.updateShape(
                                    shape.getID(),
                                    size.get(),
                                    loc.get(),
                                    shapeType,
                                    fill,
                                    null,
                                    null,
                                    textString,
                                    fontProps);
                        }
                    });
        }
    }

    /**
     * Go to next sequential page.
     */
    public void gotoNextPage() {
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
        this.gotoPage(navHistory.pop(), false);
    }

    /**
     * Go to specified page number.
     *
     * @param pageNum Number to go to.
     * @param storeHistory Should I record it in history?
     */
    public void gotoPage(final Integer pageNum, final Boolean storeHistory) {
        var pages = currentDoc.GetPages();
        pages.ifPresent(f -> gotoPage(f.get(pageNum), storeHistory));
    }

    /**
     * Go to specified page.
     *
     * @param pageID ID to go to.
     * @param storeHistory Should I record it in history?
     */
    public void gotoPage(final String pageID, final Boolean storeHistory) {
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
        if (storeHistory) {
            navHistory.push(currentPageID); // Push previous to stack
        }
        Platform.runLater(
                () -> {
                    controller.clearCard(currentPageID);
                    controller.configCard(page.getSize(),
                            page.getFillColour(), page.getID());
                });
        processEls(page);
        currentPageID = page.getID();
        putMessage("Loaded New Card: " + currentPageID, false);
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
    private void processEls(final VisualElement el) {
        // Do whatever you're going to do with this node…
        /*if (el instanceof PageElement) {
        } else*/
        if (el instanceof ImageElement) {
            this.drawImage((ImageElement) el);
        } else if (el instanceof ShapeElement) {
            this.drawShape((ShapeElement) el);
        }

        // Then recurse the children
        for (int i = 0; i < el.getChildCount(); i++) {
            var ch = el.getChild(i);
            if (ch instanceof VisualElement) {
                processEls((VisualElement) ((Element) ch));
            } else if (ch instanceof ScriptElement) { // Is the child a script?
                var chScr = ((ScriptElement) ch);
                // Make a new script engine, with the specified language
                var newScrEngine = scriptingEngineManager.
                        getEngineByName(chScr.getScriptLang());
                // Attach the correct local bindings
                newScrEngine.setBindings(el.getScriptingBindings(),
                        ScriptContext.ENGINE_SCOPE);

                try {
                    chScr.setScriptingEngine(newScrEngine);
                } catch (ScriptException ex) {
                    putMessage(
                            "Exception in script for: " + el.getID() + " is: "
                            + ex.getMessage(), Boolean.TRUE);
                }
            }
        }
    }

    /**
     * Load Tools from XML.
     *
     * @return Optional<Tools>
     */
    private Optional<Tools> loadTools() {
        var toolsXMLPath = Engine.class.getResource("tools.xml").getPath();
        var parsedDoc = ingest.
                parseGenericXML(new File(toolsXMLPath), new ToolsFactory());
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
        if (blocking) {
            Platform.runLater(() -> controller.showBlockingMessage(message));
        } else {
            Platform.runLater(() -> controller.showNonBlockingMessage(message));
        }
    }
}
