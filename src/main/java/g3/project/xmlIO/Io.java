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

import g3.project.elements.DocElement;
import g3.project.elements.ElementFactory;
import g3.project.ui.MainController;
import java.util.Optional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.*;

/**
 *
 * @author david
 */
public final class Io {

    private final static String xmlFileName = "doc.xml";

    private final static String mediaDirString = "/media";

    private final static String imagesDirString = "/images";

    private final static String scriptsDirString = "/scripts";

    private final static String tempFilePrefix = "_tmp_";
    /**
     * Open Document.
     */
    private final Optional<Document> myDoc;

    /**
     * Document Name.
     */
    private String docName;

    private File origZip;

    private FileSystem zipFs;

    private Boolean allowSave = true;

    /**
     * Create new IO and parse the project doc.
     *
     * @param presFilePath path to pres. Zip.
     */
    public Io(final String presFilePath) {

        var presFileUriString = pathToUriString(presFilePath);
        var presFileUriOpt = maybeURI(presFileUriString);
        var zipFile = presFileUriOpt.filter(uri -> uri.getPath().matches("^.*\\.(zip|ZIP|spres|SPRES)$"))
                .flatMap(Uri -> getPresArchive(Uri));
        var fsOpt = zipFile.flatMap(file -> {
            var newPath = Paths.get(System.getProperty("java.io.tmpdir") + "/" + tempFilePrefix + file.getName());
            docName = file.getName();
            origZip = file;
            try {
                Files.copy(file.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return makeFs(newPath);
        });

        myDoc = fsOpt.flatMap(fs -> {
            zipFs = fs;
            return retrieveDoc(fs);
        });
    }

    /**
     * Build from a byte array.
     *
     * @param presStream Stream containing archive.
     */
    public Io(final InputStream presStream) {
        docName = "unknown.spres";
        var tempPath = Paths.get(System.getProperty("java.io.tmpdir") + "/" + tempFilePrefix + docName);
        Optional<FileSystem> fsOpt = Optional.empty();
        try {
            var pres = presStream.readAllBytes();
            allowSave = false;
            Files.write(tempPath, pres);
            tempPath.toFile();
            fsOpt = makeFs(tempPath);
        } catch (IOException | NullPointerException ex) {
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
        }

        myDoc = fsOpt.flatMap(fs -> {
            zipFs = fs;
            return retrieveDoc(fs);
        });
        
    }

    /**
     * Get doc from FileSystem.
     *
     * @param fs FileSystem
     * @return Maybe Doc.
     */
    private Optional<Document> retrieveDoc(final FileSystem fs) {
        var docPath = fs.getPath(xmlFileName);
        try {
            var docIs = Files.newInputStream(docPath);
            return Parse.parseDocXML(docIs);
        } catch (IOException ex) {
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    /**
     * Make a new Zip FS.
     *
     * @param path Path to zip
     * @return Maybe FS.
     */
    private Optional<FileSystem> makeFs(Path path) {
        HashMap<String, String> env = new HashMap<>();
        env.put("create", "true");
        FileSystem fs = null;
        try {
            var ur = URI.create("jar:file:" + path.toAbsolutePath().toString());
            fs = FileSystems.newFileSystem(ur, env);
        } catch (IOException ex) {
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.ofNullable(fs);
    }

    /**
     * Retrieve a presentation archive.
     *
     * @param target URI of target zip.
     * @return Maybe zip file.
     */
    private Optional<File> getPresArchive(final URI target) {
        var uriScheme = target.getScheme();
        File zipFile = null;
        if (uriScheme.startsWith("file")) {
            //local
            zipFile = new File(target);
        }

        return Optional.ofNullable(zipFile);
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
     * Save document to current location.
     *
     * @throws IOException bad file.
     */
    public void save() throws IOException {
        if (allowSave == true | origZip != null) {
            saveAs(origZip.getAbsolutePath());
        } else {
            throw new IOException("Can't save.");
        }
    }

    /**
     * Save document to new location.
     *
     * @param newPath Path to save to.
     * @throws IOException bad file.
     */
    public void saveAs(final String newPath) throws IOException {
        if ( zipFs == null || myDoc.isEmpty()) {
            throw new IOException("Can't save.");
        } else if (!newPath.matches("^.*\\.(zip|ZIP|spres|SPRES)$")){
            throw new IOException("Bad File Name!");
        }
        var docPath = zipFs.getPath(xmlFileName);
        var newPathPath = Paths.get(newPath);
        var fileOutStream = Files.newOutputStream(docPath);
        Serializer serializer = new Serializer(fileOutStream, "ISO-8859-1");
        serializer.write(myDoc.get());
        //Copy the temp file to the expected place
        Files.copy(Paths.get(System.getProperty("java.io.tmpdir") + "/" + tempFilePrefix + docName),
                newPathPath,
                StandardCopyOption.REPLACE_EXISTING);
        origZip = newPathPath.toFile();
        allowSave = true;
    }

    /**
     * Get a resource from the zip.
     *
     * @param path Resource path.
     * @return Optional resource bytes.
     */
    public synchronized Optional<byte[]> getResource(final String path) {
        byte[] arr = null;
        if (path.startsWith("http")) { //Get a web resource
            try {
                var uri = new URI(path);
                var is = uri.toURL().openStream();
                arr = is.readAllBytes();
            } catch (URISyntaxException | MalformedURIException | IOException ex) {
                Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else { //Get a local resource
            var fPath = zipFs.getPath(path);
            try {
                arr = Files.readAllBytes(fPath);
            } catch (IOException ex) {
                Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Optional.ofNullable(arr);
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
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.ofNullable(arr);
    }

    /**
     * Am I allowed to save the open doc?
     *
     * @return Boolean.
     */
    public boolean canSave() {
        return this.allowSave;
    }

    /**
     * Turn a (possibly relative) path into a correct URI.
     *
     * @param path Path to convert.
     * @return Converted path.
     */
    public String pathToUriString(final String path) {
        String loc = null;
        if (path.contains(":/") || path.startsWith("/")) {
            //Must be an absolute Path
            loc = path;
        } else if (path.startsWith(".")) {
            //Must be a relative Path
            var parent = origZip.getAbsoluteFile().getParentFile().getPath();
            loc = parent.concat(path);
        }
        if (!path.startsWith("http")) {
            //Not a URL? Must be a local file
            loc = "file:".concat(loc);
        }
        return loc;
    }

    /**
     * Returns a maybe URI from an input string containing an URI.
     *
     * @param UrString URI String.
     * @return Maybe URI.
     */
    public Optional<URI> maybeURI(final String UrString) {
        URI uri = null;
        try {
            uri = new URI(UrString);
        } catch (URISyntaxException ex) {
        }

        return Optional.ofNullable(uri);
    }

    /**
     * Closes associated File Systems.
     * Must be run when object is finished with.
     */
    public void close() {
        if (zipFs != null) {
            try {
                zipFs.close();
            } catch (IOException ex) {
                Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
