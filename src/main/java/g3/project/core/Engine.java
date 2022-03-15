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
import g3.project.elements.PageElement;
import g3.project.ui.MainController;
import g3.project.xmlIO.Ingestion;
import java.io.File;
import java.util.Optional;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Point2D;

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

    private final AtomicBoolean running = new AtomicBoolean(false);
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
        engineThread.resume();
        running.set(false);
    }

    public void allowDraw() {
        UI_available.set(true);
    }

    public void offerEvent(Event event) {
        eventQueue.offer(event);
        engineThread.resume();
    }

    public void offerNewDoc(File xmlFile) {
        docQueue.offer(xmlFile);
        engineThread.resume();
    }

    @Override
    public void run() {
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
        while (iterTool.hasNext() == true){
            var currentTool = iterTool.next();
             Platform.runLater(() -> controller.addTool(currentTool.getName(), currentTool.getID(), currentTool.getScript()));
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
                    engineThread.suspend();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(
                "Engine is going down NOW.");
    }

    private void handleEvent(Event event) {
        System.out.println("g3.project.core.Engine.handleEvent()");
        System.out.println(event);
    }

    private void parseNewDoc(File xmlFile) { //Load a new doc
        var parsed = ingest.parseDocXML(xmlFile);
        if (parsed.isPresent()) {
            var child = parsed.get().getChild(0);
            if (child instanceof DocElement) {
                currentDoc = (DocElement) child;
                currentDoc.GetPages().ifPresent(f -> {
                    currentPages = f;
                });
                drawPage(0);
            } else {
                //Looks like doc is malformed
            }
            System.out.println("New document loaded");
        } else {
            //Oops, couldn't parse initial doc.
        }
    }

    private void drawPage(Integer pageNum) {
        var pages = currentDoc.GetPages();
        pages.ifPresent(f -> drawPage(f.get(pageNum)));
    }

    private void drawPage(String pageID) {
        var it = currentPages.iterator();
        while (it.hasNext()) {
            var page=it.next();
            if (page.getID() == pageID) {
                drawPage(page);
            }
        }
    }

    private void drawPage(PageElement page) {
        Platform.runLater(() -> {
            controller.configPage(page.getSize(), page.getFillColour(), page.getID());
        });
    }

    private Optional<Tools> loadTools() {
        var toolsXMLPath = Engine.class.getResource("tools.xml").getPath();
        var parsedDoc = ingest.parseGenericXML(new File(toolsXMLPath), new ToolsFactory());
        var root = parsedDoc.filter(d -> d.getRootElement() instanceof Tools)
                .map(d -> (Tools) d.getRootElement());
        return root;
    }

}
