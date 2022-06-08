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

import nu.xom.Document;
import nu.xom.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
<<<<<<< Updated upstream
 *
 * @author david
=======
 * @author Group 3
>>>>>>> Stashed changes
 */
public class DocIO extends IO {
    
    protected final static String xmlFileName = "doc.xml";

    public DocIO(final String presFilePath) {
        super(presFilePath);
    }

    public DocIO(final InputStream presStream) {
        super(presStream);
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
        if (zipFs == null || myDoc.isEmpty()) {
            throw new IOException("Can't save.");
        } else if (!newPath.matches("^.*\\.(zip|ZIP|spres|SPRES)$")) {
            throw new IOException("Bad File Name!");
        }
        Path docPath = zipFs.getPath(xmlFileName);
        Path tmpDocPath = Files.createTempFile("_tmpdoc", "");
        Files.deleteIfExists(docPath);
        var fileOutStream = Files.newOutputStream(tmpDocPath);
        Serializer serializer = new Serializer(fileOutStream, "ISO-8859-1");
        serializer.write(myDoc.get());
        Files.move(tmpDocPath, docPath, StandardCopyOption.REPLACE_EXISTING);
        //Close and reopen (sync). This is undocumented!!!
        zipFs.close();
        makeFs(tempPath).ifPresent(fs -> zipFs = fs);
        var newPathPath = Paths.get(newPath);
        //Copy the temp file to the expected place
        Files.copy(tempPath,
                newPathPath,
                StandardCopyOption.REPLACE_EXISTING);
        origZip = newPathPath.toFile();
        allowSave = true;
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
<<<<<<< Updated upstream
     * Get an internal class-path resource as bytes.
=======
     * Try to get an empty file in the given location, with the given prefix and
     * suffix.
     *
     * @param loc    Location to create file.
     * @param prefix File Prefix.
     * @param suffix File Suffix.
     * @return New File Path.
     * @throws IOException Couldn't make file.
     */
    public Path getEmptyFile(final String loc, final String prefix, final String suffix) throws IOException {
        var containingPath = zipFs.getPath(loc);
        Files.createDirectories(containingPath);
        var filePath = Files.createTempFile(containingPath, prefix, suffix);
        return filePath;
    }

    /**
     * Add a resource to the zip.
>>>>>>> Stashed changes
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
            Logger.getLogger(DocIO.class.getName()).log(Level.SEVERE, null, ex);
        }
<<<<<<< Updated upstream
        return Optional.ofNullable(arr);
=======
        return getResource(newPath);
    }

    /**
     * Write to a given file.
     *
     * @param path    Path to file.
     * @param content File Content.
     * @throws java.io.IOException Couldn't access path.
     */
    public void writeBytes(final String path, final byte[] content) throws IOException {
        var filePath = zipFs.getPath(path);
        Files.write(filePath, content);
>>>>>>> Stashed changes
    }

    @Override
    protected Optional<Document> retrieveDoc(FileSystem fs) {
        var docPath = fs.getPath(xmlFileName);
        try {
            var docIs = Files.newInputStream(docPath);
            return Parse.parseDocXML(docIs);
        } catch (IOException ex) {
            Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }
}
