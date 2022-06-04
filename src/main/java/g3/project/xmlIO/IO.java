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

import java.util.Optional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.*;

/**
 *
 * @author david
 */
public abstract class IO {
    //CHECKSTYLE:OFF
    protected final static String mediaDirString = "/media";

    protected final static String imagesDirString = "/images";

    protected final static String scriptsDirString = "/scripts";

    protected final static String tempFilePrefix = "_sprestmp_";

    /**
     * Document Name.
     */
    protected String docName;

    protected File origZip;

    /**
     * Open Document.
     */
    protected final Optional<Document> myDoc;

    protected FileSystem zipFs;

    protected Path tempPath;

    protected Boolean allowSave = false;
    //CHECKSTYLE:ON

    /**
     * Temporary files requiring cleanup.
     */
    protected final HashMap<String, Path> tempFiles = new HashMap<>();

    /**
     * Create new IO and parse the project doc.
     *
     * @param presFilePath path to pres. Zip.
     */
    public IO(final String presFilePath) {

        var presFileUriString = pathToUriString(presFilePath);
        var presFileUriOpt = maybeURI(presFileUriString);
        var zipFile = presFileUriOpt.filter(uri -> uri.getPath().matches("^.*\\.(zip|ZIP|spres|SPRES)$"))
                .flatMap(Uri -> getArchive(Uri)); //Retrieve the Zip archive.
        var fsOpt = zipFile.flatMap(file -> {
            docName = file.getName();
            origZip = file;
            allowSave = true;
            try {
                tempPath = Files.createTempFile(tempFilePrefix, "");
                Files.copy(file.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);
                tempFiles.put(docName, tempPath);
            } catch (IOException ex) {
                Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return makeFs(tempPath);
        }); //Get the zip file-system. Maybe.

        myDoc = fsOpt.flatMap(fs -> {
            zipFs = fs;            
            return retrieveDoc(fs);
        }); //Maybe get the doc.
    }

    /**
     * Build from a byte array.
     *
     * @param presStream Stream containing archive.
     */
    public IO(final InputStream presStream) {
        docName = "unknown.spres";
        Optional<FileSystem> fsOpt = Optional.empty();
        try {
            tempPath = Files.createTempFile(tempFilePrefix, "");
            tempFiles.put(docName, tempPath);
            var pres = presStream.readAllBytes();
            allowSave = false;
            Files.write(tempPath, pres);
            fsOpt = makeFs(tempPath);
        } catch (IOException | NullPointerException ex) {
            Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
        }

        myDoc = fsOpt.flatMap(fs -> {
            zipFs = fs;
            return retrieveDoc(fs);
        });
    }

    /**
     * Get doc from FileSystem. Must be implemented by subclass!
     *
     * @param fs FileSystem
     * @return Maybe Doc.
     */
    protected abstract Optional<Document> retrieveDoc(final FileSystem fs);

    /**
     * Make a new Zip FS.
     *
     * @param path Path to zip
     * @return Maybe FS.
     */
    protected static final Optional<FileSystem> makeFs(final Path path) {
        HashMap<String, String> env = new HashMap<>();
        env.put("create", "true");
        FileSystem fs = null;
        try {
            var urStr = path.toAbsolutePath().toFile().toURI().toString();
            var ur = URI.create("jar:" + urStr);
            fs = FileSystems.newFileSystem(ur, env);
        } catch (IOException ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.ofNullable(fs);
    }

    /**
     * Retrieve a presentation archive.
     *
     * @param target URI of target zip.
     * @return Maybe zip file.
     */
    protected static final Optional<File> getArchive(final URI target) {
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
     * Get a resource from the zip.
     *
     * @param path Resource path.
     * @return Optional resource bytes.
     */
    public synchronized Optional<byte[]> getResource(final String path) {
        byte[] arr = null;
        if (isUriInternal(path)) { //Get an internal resource
            var fPath = zipFs.getPath(path);
            try {
                arr = Files.readAllBytes(fPath);
            } catch (IOException ex) {
                
            }
        } else { //Get an external resource
            try {
                var uri = new URI(path);
                var is = uri.toURL().openStream();
                arr = is.readAllBytes();
            } catch (URISyntaxException | MalformedURIException | IOException ex) {
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
                Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
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
        //CHECKSTYLE:OFF
        if (path.startsWith("http")) {
            return false;
        } else if (path.startsWith("file:")) {
            return false;
        } else {
            return true;
        }
        //CHECKSTYLE:ON
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
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
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
        if (path.contains(":/") || path.startsWith("/") || path.contains(":\\")) {
            //Must be an absolute Path
            loc = path;
        } else if (path.startsWith(".")) {
            //Must be a relative Path
            var parent = origZip.getAbsoluteFile().getParentFile().getPath();
            loc = parent.concat(path);
        }
        if (!path.startsWith("http")) {
            //Not a URL? Must be a local file
            var p = Paths.get(path);
            loc = p.toUri().toString();
        }
        return loc;
    }

    /**
     * Returns a maybe URI from an input string containing an URI.
     *
     * @param UrString URI/Path String.
     * @return Maybe URI.
     */
    public Optional<URI> maybeURI(final String UrString) {
        URI uri = null;

        try {
            uri = new URI(UrString);
        } catch (URISyntaxException ex) {
            System.err.println(ex);
        }

        return Optional.ofNullable(uri);
    }

    /**
     * Closes associated File Systems. Must be run when object is finished with.
     */
    public void close() {
        if (zipFs != null) {
            try {
                zipFs.close();
            } catch (IOException ex) {
                Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        tempFiles.forEach((id, p) -> {
            p.toFile().delete();
        });
    }
}
