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

<<<<<<< Updated upstream
import g3.project.elements.DocElement;
import g3.project.elements.ElementFactory;
import g3.project.ui.MainController;
import java.io.BufferedWriter;
import java.util.Optional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
=======
import nu.xom.Document;

import java.io.IOException;
import java.io.InputStream;
>>>>>>> Stashed changes
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
<<<<<<< Updated upstream
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;
=======
import java.util.Optional;
>>>>>>> Stashed changes
import java.util.logging.Level;
import java.util.logging.Logger;

/**
<<<<<<< Updated upstream
 *
 * @author david
 */
public final class ToolIO extends IO {

    protected final static String xmlFileName = "tools.xml";
=======
 * @author Group 3
 */
public final class ToolIO extends IO {
    /**
     * Name of XML doc.
     */
    protected final static String XML_FILE_NAME = "tools.xml";
>>>>>>> Stashed changes

    /**
     * Load tools from path string.
     *
     * @param toolFilePath Path to tools Zip.
     */
    public ToolIO(final String toolFilePath) {
        super(toolFilePath);
    }

    /**
     * Load tools from Stream.
     *
     * @param toolStream Stream of tools Zip.
     */
    public ToolIO(final InputStream toolStream) {
        super(toolStream);
    }

    /**
     * Get doc from FileSystem.
     *
     * @param fs FileSystem
     * @return Maybe Doc.
     */
    @Override
    protected Optional<Document> retrieveDoc(final FileSystem fs) {
        var docPath = fs.getPath(xmlFileName);
        try {
            var docIs = Files.newInputStream(docPath);
            return Parse.parseToolXML(docIs);
        } catch (IOException ex) {
            Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    /**
     * Get parsed document.
     *
     * @return Optional document.
     */
    public Optional<Document> getDoc() {
        return myDoc;
    }

    /**
     * Get a resource from the zip.
     *
     * @param path Resource path.
     * @return Optional resource bytes.
     */
    public synchronized Optional<byte[]> getResource(final String path) {
        if (path.isEmpty()) {
            return Optional.empty();
        }
        byte[] arr = null;
        if (isUriInternal(path)) { //Get an internal resource
            var fPath = zipFs.getPath(path);
            try {
                arr = Files.readAllBytes(fPath);
            } catch (IOException ex) {
                Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else { //Get an external resource
            try {
                var uri = new URI(path);
                var is = uri.toURL().openStream();
                arr = is.readAllBytes();
            } catch (URISyntaxException | MalformedURIException | IOException ex) {
                Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Optional.ofNullable(arr);
    }

    /**
     * Extract a resource from the zip and return its path.
     *
     * @param path Resource path.
     * @return Optional resource bytes.
     */
    public synchronized Optional<String> getResourceTempPath(final String path) {
        if (isUriInternal(path)) { //Get an internal resource
            var cached = tempFiles.get(path); //Have we seen it before?
            if (cached != null) {
                return Optional.of(cached.toAbsolutePath().toString());
            }
            var fPath = zipFs.getPath(path);
            try {
                var tempfPath = Files.createTempFile(tempFilePrefix, "");
                Files.copy(fPath, tempfPath, StandardCopyOption.REPLACE_EXISTING);
                tempFiles.put(path, tempfPath);
                return Optional.of(tempfPath.toAbsolutePath().toString());
            } catch (IOException ex) {
                Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
                return Optional.empty();
            }
        } else { //External resource. Return input.
            return Optional.of(path);
        }
    }

    /**
     * Check if given Path should be in the ZIP archive.
     *
     * @param path Path to check.
     * @return True or False.
     */
    public static Boolean isUriInternal(final String path) {
        if (path.startsWith("http")) {
            return false;
        } else if (path.startsWith("file:")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Add a resource to the zip.
     *
     * @param exrPath Existing Resource path.
     * @param newPath Path within zip.
     * @return Optional resource bytes.
     */
    public synchronized Optional<byte[]> addResource(final String exrPath, final String newPath) {
        var internalPath = zipFs.getPath(newPath);
        var resPath = Paths.get(exrPath);
        try {
            Files.copy(resPath, internalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
            return Optional.empty();
        }
        return getResource(newPath);
    }

    /**
     * Get an internal class-path resource as bytes.
     *
     * @param file File to return.
     * @param resClass Class to look in.
     * @return Maybe file bytes.
     */
    public static Optional<byte[]> getInternalResource(final String file, final Class resClass) {
        byte[] arr = null;
        var is = resClass.getResourceAsStream(file);
        try {
            arr = is.readAllBytes();
        } catch (IOException ex) {
            Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.ofNullable(arr);
    }

    /**
     * Am I allowed to save the open doc?
     *
     * @return Boolean.
     */
    @Override
    public boolean canSave() {
        return false;
    }
}
