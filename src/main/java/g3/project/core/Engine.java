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
import g3.project.ui.MainController;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;

/**
 *
 * @author david
 */
public class Engine implements Runnable {
    private Thread engineThread;
    private DocElement currentDoc;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private final BlockingQueue<String> editedElementQueue; //UI has edited an element
    private final BlockingQueue<String> newElementQueue; //UI requests element is created
    //private final BlockingQueue<String> redrawElementQueue; //Engine requests on-screen element be updated

    private final BlockingQueue<DocElement> docQueue; //Operate on this document

    private final MainController controller;

    public Engine(BlockingQueue<String> editedElementQueue, BlockingQueue<String> newElementQueue, BlockingQueue<DocElement> docQueue, MainController uiController) {
        this.editedElementQueue = editedElementQueue;
        this.newElementQueue = newElementQueue;
        //this.redrawElementQueue = redrawElementQueue;
        this.docQueue = docQueue;
        this.controller = uiController;
    }
    
    private Integer loadDoc(DocElement doc) {

        return 0;
    }
    
    public void start(){
        engineThread = new Thread(this);
        engineThread.start();
        running.set(true);
    }
    
    public void stop(){
        running.set(false);
    }
    
    @Override
    public void run() {
        while (running.get() == true) {
            if (!newElementQueue.isEmpty()) {
                try {
                System.out.println("hello from engine");
                final String el = newElementQueue.take();
                Platform.runLater(() -> {
                    controller.drawText(el, new Point2D(0, 0));
                    System.out.println("run later");
                });//"Hello from the other side"
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                
            }
        }
    }

}
