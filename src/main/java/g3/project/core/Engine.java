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
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import nu.xom.Document;

/**
 *
 * @author david
 */
public class Engine implements Runnable {

    private Thread engineThread;

    private Ingestion ingest = new Ingestion();

    private DocElement currentDoc;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean UI_available = new AtomicBoolean(false);

    private final BlockingQueue<String> editedElementQueue = new LinkedBlockingQueue<String>(); //UI has edited an element
    private final BlockingQueue<ActionEvent> actionQueue = new LinkedBlockingQueue<ActionEvent>(); //UI has edited an element
    private final BlockingQueue<String> newElementQueue = new LinkedBlockingQueue<String>(); //UI requests element is created    
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

    public void offerEditedElement(String element) {
        editedElementQueue.offer(element);
        engineThread.resume();
    }

    public void offerAction(ActionEvent action) {
        actionQueue.offer(action);
        engineThread.resume();
    }

    public void offerNewElement(String element) {
        newElementQueue.offer(element);
        engineThread.resume();
    }

    public void offerNewDoc(File xmlFile) {
        docQueue.offer(xmlFile);
        engineThread.resume();
    }

    @Override
    public void run() {
        Integer currentPageNum = 0;

        while (running.get()
                == true) {
            try {
                if (!newElementQueue.isEmpty()) {
                    System.out.println("hello from engine");
                    final String el = newElementQueue.take();
                    Platform.runLater(() -> {
                        controller.drawText(el, new Point2D(0, 0));
                        System.out.println("run later");
                    });//"Hello from the other side"

                } else if (!docQueue.isEmpty()) {
                    parseNewDoc(docQueue.take());
                } else if (!editedElementQueue.isEmpty()) {

                } else if (!actionQueue.isEmpty()) {
                    handleAction(actionQueue.take());
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

    private void handleAction(ActionEvent action) {

    }

    private void parseNewDoc(File xmlFile) {
        var parsed = ingest.parseDocXML(xmlFile);
        if (parsed.isPresent()) {
            var child = parsed.get().getChild(0);
            if (child.getClass() == DocElement.class) {
                currentDoc = (DocElement) child;
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
        var pages_op = currentDoc.GetPages();
        if (pages_op.isPresent()) {
            Vector<PageElement> pages = pages_op.get();
            var it = pages.iterator();
            while (it.hasNext()) {
                var page = it.next();
                page.getID().ifPresent(f -> {
                    if (f==pageID){
                        drawPage(page);
                    }
                });
            }
        }
    }

    private void drawPage(PageElement page) {
        page.getSize().ifPresent(f->controller.setPageSize(f));
        
    }

}
