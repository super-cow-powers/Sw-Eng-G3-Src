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

import g3.project.elements.DocElement;
import g3.project.elements.*;
import g3.project.elements.PageElement;
import g3.project.elements.VisualElement;
import g3.project.graphics.FontProps;
import g3.project.ui.LocObj;
import g3.project.ui.MainController;
import g3.project.ui.SizeObj;
import g3.project.xmlIO.Ingestion;
import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import nu.xom.Element;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author david
 */
public class Engine implements Runnable {

    private Thread engineThread;

    private Ingestion ingest = new Ingestion();

    private ArrayList<Tool> myTools;
    private DocElement currentDoc;
    private ArrayList<PageElement> currentPages;
    private String currentPageID = "";
    
    private ScriptEngineManager scriptingEngineManager;
    
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean suspended = new AtomicBoolean(false);
    private final AtomicBoolean UI_available = new AtomicBoolean(false);

    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(); //Something has happened
    private final BlockingQueue<File> docQueue = new LinkedBlockingQueue<File>(); //Open new doc/s

    private final MainController controller;

    public Engine(MainController uiController) {
        this.controller = uiController;
    }

    public void start() {
        engineThread = new Thread(this);
        engineThread.start();
        running.set(true);
    }

    public void stop() {
        running.set(false);
        unsuspend();
    }

    public void allowDraw() {
        UI_available.set(true);
    }

    public void offerEvent(Event event) {
        eventQueue.offer(event);
        unsuspend();
    }

    public void offerNewDoc(File xmlFile) {
        docQueue.offer(xmlFile);
        unsuspend();
    }

    //Trigger notify if suspended
    private synchronized void unsuspend() {
        if (suspended.get() == true) {
            suspended.set(false);
            notify();
        }
    }

    @Override
    public void run() {
        //Load Script engine
        scriptingEngineManager = new ScriptEngineManager();
        while (running.get() == false) {
        };
        //Load in the tools
        loadTools().ifPresentOrElse(t -> myTools = t.getTools(),
                () -> {
                    myTools = new ArrayList<Tool>();
                    Platform.runLater(() -> controller.showNonBlockingMessage("Failed Loading Tools!"));
                }
        );
        //Add tool buttons
        var iterTool = myTools.iterator();
        while (iterTool.hasNext() == true) {
            var currentTool = iterTool.next();
            Platform.runLater(() -> controller.addTool(currentTool.getName(), currentTool.getID()));
        }
        //Quit if running flag set to false
        while (running.get()
                == true) {
            try {
                if (!docQueue.isEmpty()) { //New doc request?
                    parseNewDoc(docQueue.take());
                } else if (!eventQueue.isEmpty()) { //New event?
                    handleEvent(eventQueue.take());
                } else {
                    suspended.set(true);
                }

                while (suspended.get() == true) { //Suspend
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(
                "Engine is going down NOW.");
        return;
    }

    private void handleEvent(Event event) {
        System.out.println("g3.project.core.Engine.handleEvent()");
        System.out.println(event);
        var evTgt = event.getTarget();
        if (evTgt instanceof Button) {
            handleButtonEvent(event);
        } else
        if (event instanceof KeyEvent) {
            var kev = (KeyEvent) event;
            var current_card = getPageIndex(currentPageID);
            if (kev.getCode() == KeyCode.LEFT) {
                if (current_card > 0){
                    current_card--;
                }
                this.drawPage(current_card);
            } else if (kev.getCode() == KeyCode.RIGHT) {
                if (current_card < currentPages.size()-1){
                    current_card++;
                }
                this.drawPage(current_card);
            }
        }
        
        event.consume();
    }

    private void handleButtonEvent(Event ev) {
        if (ev instanceof ActionEvent) {
            var aev = (ActionEvent) ev;
            var target = aev.getTarget();
            if (target instanceof Button) {
                if (((Button) target).getId().contains("-jump-card-button")) {
                    var ID = ((Button) target).getId().replace("-jump-card-button", "");
                    this.drawPage(ID);
                }
            }
        }
    }

    private void handleNavButtonEvent(ActionEvent aev) {

    }

    private void parseNewDoc(File xmlFile) { //Load a new doc

        var parsed = ingest.parseDocXML(xmlFile);
        if (parsed.isPresent()) {
            Platform.runLater(() -> {
                controller.clearCardButtons();
                controller.clearCard("");
                controller.setViewScale(1d);
            });
            var child = parsed.get().getChild(0);
            if (child instanceof DocElement) {
                currentDoc = (DocElement) child;
                currentDoc.GetPages().ifPresent(f -> {
                    currentPages = f;
                });
                var it = currentPages.listIterator(); //Add buttons for each page
                while (it.hasNext()) {
                    var ind = it.nextIndex();
                    var page = it.next();
                    Platform.runLater(() -> {
                        var tiopt = page.getTitle();
                        var ID = page.getID();
                        var title = tiopt.isPresent() ? tiopt.get() : ID;
                        controller.addCardButton(title, ID, ind);
                    });
                }
                drawPage(currentPages.get(0).getID());

            } else {
                //Looks like doc is malformed
            }
            System.out.println("New document loaded");
        } else {
            //Oops, couldn't parse initial doc.
        }
    }

    public void drawImage(ImageElement img) {
        var source_opt = img.getSourceLoc();
        var loc_opt = img.getLoc();
        var size_opt = img.getSize();
        var ID = img.getID();
        Platform.runLater(() -> {
            var source = (source_opt.isPresent()) ? source_opt.get() : "";
            var loc = (loc_opt.isPresent()) ? loc_opt.get() : new LocObj(new Point2D(0, 0), null, null, 0d);
            var size = (size_opt.isPresent()) ? size_opt.get() : new SizeObj(20d, 20d, 0d);

            controller.updateImage(ID, size, loc, source);

        });
    }

    public void drawShape(ShapeElement shape) {
        Platform.runLater(() -> {

            ArrayList<FontElement> font_blocks = new ArrayList<>();
            FontProps fontProps;
            String textString;
            var size = shape.getSize();
            var loc = shape.getLoc();
            var shapeType = shape.getType();
            var fill_op = shape.getFillColour();
            Color fill;
            if (fill_op.isPresent()) {
                fill = fill_op.get();
            } else {
                fill = Color.WHITESMOKE;
            }
            var text_op = shape.getText();
            if (text_op.isPresent()) {
                font_blocks = text_op.get().getFontBlocks();
            }

            if (font_blocks.size() > 0) {
                textString = font_blocks.get(0).getValue();
                fontProps = font_blocks.get(0).getProperties();
            } else {
                fontProps = null;
                textString = "";
            }
            if (size.isPresent() && loc.isPresent()) {
                Platform.runLater(() -> {
                    if (fontProps != null) {
                        controller.updateShape(shape.getID(), size.get(), loc.get(), shapeType, fill, null, null, textString, fontProps);
                    }
                });
            }
        }
        );
    }

    public void drawPage(Integer pageNum) {
        var pages = currentDoc.GetPages();
        pages.ifPresent(f -> drawPage(f.get(pageNum)));
    }

    public void drawPage(String pageID) {
        var it = currentPages.iterator();
        while (it.hasNext()) {
            var page = it.next();
            var itID = page.getID();
            if (itID.equals(pageID)) {
                drawPage(page);
                putMessage("Loaded New Card: " + pageID, false);
            }
        }
    }

    public void drawPage(PageElement page) {
        Platform.runLater(() -> {
            controller.clearCard(currentPageID);
            controller.configCard(page.getSize(), page.getFillColour(), page.getID());
        });
        processEls(page);
        currentPageID = page.getID();
    }

    private Integer getPageIndex(String pageID) {
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

    //Process the elements on a page
    private void processEls(VisualElement el) {

        if (el instanceof PageElement) {

        } else if (el instanceof ImageElement) {
            this.drawImage((ImageElement) el);
        } else if (el instanceof ShapeElement) {
            this.drawShape((ShapeElement) el);
        }
        // Do whatever you're going to do with this node…
        // recurse the children
        for (int i = 0; i < el.getChildCount(); i++) {
            var ch = el.getChild(i);
            if (ch instanceof VisualElement) {
                processEls((VisualElement) ((Element) ch));
            }
        }

    }

    private Optional<Tools> loadTools() {
        var toolsXMLPath = Engine.class.getResource("tools.xml").getPath();
        var parsedDoc = ingest.parseGenericXML(new File(toolsXMLPath), new ToolsFactory());
        var root = parsedDoc.filter(d -> d.getRootElement() instanceof Tools)
                .map(d -> (Tools) d.getRootElement());
        this.putMessage("Tools Loaded", false);
        return root;
    }

    /**
     * Instruct the UI to show a message to the User
     *
     * @param message
     * @param blocking
     */
    private void putMessage(String message, Boolean blocking) {
        if (blocking) {
            //Not implemented
        } else {
            Platform.runLater(() -> controller.showNonBlockingMessage(message));
        }

    }
}
