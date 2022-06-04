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
package g3.project.xmlIO;

import g3.project.ui.MainController;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Document;
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
public class DocIOTest {
//CHECKSTYLE:OFF

    static DocIO instance;

    public DocIOTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        Path path;
        try {
            path = Path.of(MainController.class.getResource("test_doc.spres").toURI());
        } catch (URISyntaxException ex) {
            Logger.getLogger(DocIOTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Couldn't load test file.");
            return;
        }
        instance = new DocIO(path.toString());
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
    //CHECKSTYLE:ON

    /**
     * Test of saveAs method, of class DocIO.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSaveAs() throws Exception {
        System.out.println("saveAs");
        String newPath = System.getProperty("java.io.tmpdir") + "/test.spres";
        instance.saveAs(newPath);
        assertTrue(Files.exists(Paths.get(System.getProperty("java.io.tmpdir") + "/test.spres")));
        Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir") + "/test.spres"));
    }

    /**
     * Test of isUriInternal method, of class DocIO.
     */
    @Test
    public void testIsUriInternal() {
        System.out.println("isUriInternal");
        ArrayList<String> intPaths = new ArrayList<>(Arrays.asList("/images/test.jpg", "scripts/scr.py"));
        ArrayList<String> extPaths = new ArrayList<>(Arrays.asList("http://www.google.com", "https://www.ibm.com", "file:/home/david/doc.txt"));
        for (var path : intPaths) {
            assertTrue(DocIO.isUriInternal(path));
        }
        for (var path : extPaths) {
            assertFalse(DocIO.isUriInternal(path));
        }
    }

    /**
     * Test of removeResource method, of class DocIO.
     */
    @Test
    public void testRemoveResource() {
    }

    /**
     * Test of getEmptyFile method, of class DocIO.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetEmptyFile() throws Exception {
        System.out.println("getEmptyFile");
        var path = instance.getEmptyFile("/", "", "");
        var pathStr = path.toString();
        var res = instance.getResource(pathStr);
        assertTrue(res.isPresent()); //Does the file exist
        Files.deleteIfExists(instance.zipFs.getPath(pathStr));
    }

    /**
     * Test of addResource method, of class DocIO.
     * @throws java.lang.Exception
     */
    @Test
    public void testAddResource() throws Exception {
        System.out.println("addResource");
        var logo = MainController.class.getResource("logo.jpg");
        var logoUri = logo.toURI();
        var logoBytes = logo.openStream().readAllBytes();
        var logoPath = Path.of(logoUri);
        var newLogoPath = "/logo.jpg";
        instance.addResource(logoPath.toAbsolutePath().toString(), newLogoPath);
        var res = instance.getResource(newLogoPath);
        assertTrue(res.isPresent()); //Have I got bytes back?
        res.ifPresent(r -> assertEquals(new String(r, StandardCharsets.UTF_8), new String(logoBytes, StandardCharsets.UTF_8))); //Are the bytes right?
        Files.deleteIfExists(instance.zipFs.getPath(newLogoPath));
    }

    /**
     * Test of writeBytes method, of class DocIO.
     * @throws java.lang.Exception
     */
    @Test
    public void testWriteBytes() throws Exception {
        System.out.println("writeBytes");
        String path = "/tstfile";
        String str = "hello";
        byte[] content = str.getBytes();
        instance.writeBytes(path, content);
        var res = instance.getResource(path);
        assertTrue(res.isPresent()); //Have I got bytes back?
        res.ifPresent(r -> assertEquals(new String(r, StandardCharsets.UTF_8), new String(content, StandardCharsets.UTF_8))); //Are the bytes right?
        Files.deleteIfExists(instance.zipFs.getPath(path));
    }

    /**
     * Test of retrieveDoc method, of class DocIO.
     */
    @Test
    public void testRetrieveDoc() {
        System.out.println("retrieveDoc");
        Path path;
        try {
            path = Path.of(MainController.class.getResource("test_doc.spres").toURI());
        } catch (URISyntaxException ex) {
            Logger.getLogger(DocIOTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Couldn't load test file.");
            return;
        }
        FileSystem fs = IO.makeFs(path).get();
        Optional<Document> result = instance.retrieveDoc(fs);
        assertTrue(result.isPresent());
    }
}
