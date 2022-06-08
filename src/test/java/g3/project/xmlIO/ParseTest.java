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
package g3.project.xmlIO;

import g3.project.core.Engine;
import g3.project.elements.DocElement;
import g3.project.ui.MainController;
import nu.xom.Document;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Group 3
 */
public class ParseTest {
//CHECKSTYLE:OFF

    public ParseTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of parseDocXML method, of class Parse.
     */
    @Test
    public void testParseDocXML_File() {
        System.out.println("parseDocXML");
        File xmlFile;
        try {
            xmlFile = new File(MainController.class.getResource("test.xml").toURI());
        } catch (URISyntaxException ex) {
            Logger.getLogger(ParseTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Couldn't open test file!");
            return;
        }
        Optional<Document> result = Parse.parseDocXML(xmlFile);
        assertTrue(result.isPresent());
        result.ifPresent(d -> assertTrue(d.getRootElement() instanceof DocElement));
        //Try invalid doc
        try {
            xmlFile = new File(MainController.class.getResource("test_inv.xml").toURI());
        } catch (URISyntaxException ex) {
            Logger.getLogger(ParseTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Couldn't open test file!");
            return;
        }
        result = Parse.parseDocXML(xmlFile);
        assertTrue(result.isEmpty());
    }

    /**
     * Test of parseDocXML method, of class Parse.
     */
    @Test
    public void testParseDocXML_InputStream() {
        System.out.println("parseDocXML");
        InputStream xmlStream;
        try {
            xmlStream = MainController.class.getResourceAsStream("test.xml");
        } catch (Exception ex) {
            Logger.getLogger(ParseTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Couldn't open test file!");
            return;
        }
        Optional<Document> result = Parse.parseDocXML(xmlStream);
        assertTrue(result.isPresent());
        result.ifPresent(d -> assertTrue(d.getRootElement() instanceof DocElement));
        //Try invalid doc
        try {
            xmlStream = MainController.class.getResourceAsStream("test_inv.xml");
        } catch (Exception ex) {
            Logger.getLogger(ParseTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Couldn't open test file!");
            return;
        }
        result = Parse.parseDocXML(xmlStream);
        assertTrue(result.isEmpty());
    }

    /**
     * Test of parseToolXML method, of class Parse.
     */
    @Test
    public void testParseToolXML() {
        InputStream xmlStream;
        try {
            xmlStream = Engine.class.getResourceAsStream("test_tools.xml");
        } catch (Exception ex) {
            Logger.getLogger(ParseTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Couldn't open test file!");
            return;
        }
        Optional<Document> result = Parse.parseDocXML(xmlStream);
        assertTrue(result.isPresent());
        result.ifPresent(d -> assertTrue(d.getRootElement().getLocalName().equals("tools")));
        //Try an invalid doc
        try {
            xmlStream = Engine.class.getResourceAsStream("test_tools_inv.xml");
        } catch (Exception ex) {
            Logger.getLogger(ParseTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Couldn't open test file!");
            return;
        }
        result = Parse.parseDocXML(xmlStream);
        assertTrue(result.isEmpty());
    }
//CHECKSTYLE:ON
}
