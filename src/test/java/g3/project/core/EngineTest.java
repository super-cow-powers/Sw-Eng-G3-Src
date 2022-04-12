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

import g3.project.elements.ImageElement;
import g3.project.elements.PageElement;
import g3.project.elements.ShapeElement;
import java.io.File;
import javafx.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class EngineTest {
    
    public EngineTest() {
    }

    @org.junit.jupiter.api.BeforeAll
    public static void setUpClass() throws Exception {
    }

    @org.junit.jupiter.api.AfterAll
    public static void tearDownClass() throws Exception {
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUp() throws Exception {
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
    }
    

    /**
     * Test of start method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testStart() {
        System.out.println("start");
        Engine instance = null;
        instance.start();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stop method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testStop() {
        System.out.println("stop");
        Engine instance = null;
        instance.stop();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of allowDraw method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testAllowDraw() {
        System.out.println("allowDraw");
        Engine instance = null;
        instance.allowDraw();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of offerEvent method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testOfferEvent() {
        System.out.println("offerEvent");
        Event event = null;
        Engine instance = null;
        instance.offerEvent(event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of offerNewDoc method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testOfferNewDoc() {
        System.out.println("offerNewDoc");
        File xmlFile = null;
        Engine instance = null;
        instance.offerNewDoc(xmlFile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of run method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testRun() {
        System.out.println("run");
        Engine instance = null;
        instance.run();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of drawImage method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testDrawImage() {
        System.out.println("drawImage");
        ImageElement img = null;
        Engine instance = null;
        instance.drawImage(img);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of drawShape method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testDrawShape() {
        System.out.println("drawShape");
        ShapeElement shape = null;
        Engine instance = null;
        instance.drawShape(shape);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of gotoNextPage method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testGotoNextPage() {
        System.out.println("gotoNextPage");
        Engine instance = null;
        instance.gotoNextPage();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of gotoPrevPage method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testGotoPrevPage() {
        System.out.println("gotoPrevPage");
        Engine instance = null;
        instance.gotoPrevPage();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of gotoPage method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testGotoPage_Integer_Boolean() {
        System.out.println("gotoPage");
        Integer pageNum = null;
        Boolean storeHistory = null;
        Engine instance = null;
        instance.gotoPage(pageNum, storeHistory);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of gotoPage method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testGotoPage_String_Boolean() {
        System.out.println("gotoPage");
        String pageID = "";
        Boolean storeHistory = null;
        Engine instance = null;
        instance.gotoPage(pageID, storeHistory);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of gotoPage method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testGotoPage_PageElement_Boolean() {
        System.out.println("gotoPage");
        PageElement page = null;
        Boolean storeHistory = null;
        Engine instance = null;
        instance.gotoPage(page, storeHistory);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of putMessage method, of class Engine.
     */
    @org.junit.jupiter.api.Test
    public void testPutMessage() {
        System.out.println("putMessage");
        String message = "";
        Boolean blocking = null;
        Engine instance = null;
        instance.putMessage(message, blocking);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
