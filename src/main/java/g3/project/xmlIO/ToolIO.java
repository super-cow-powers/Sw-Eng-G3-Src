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

import java.util.Optional;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.*;

/**
 *
 * @author Group 3
 */
public final class ToolIO extends IO {
/**
 * Name of XML doc.
 */
    protected final static String XML_FILE_NAME = "tools.xml";

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
        var docPath = fs.getPath(XML_FILE_NAME);
        try {
            var docIs = Files.newInputStream(docPath);
            return Parse.parseToolXML(docIs);
        } catch (IOException ex) {
            Logger.getLogger(ToolIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
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
