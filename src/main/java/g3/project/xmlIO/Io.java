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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import nu.xom.*;

/**
 *
 * @author david
 */
public final class Io {

    private static final String PWS = "PWS.xsd";
    public static final String PWS_NS = "PWS_Base";
    private static final String EXT_SCHEMA = "my_exts.xsd";
    public static final String EXT_NS = "PWS_Exts";
    /**
     * Open Document.
     */
    private final Optional<Document> myDoc;

    /**
     * Document Name.
     */
    private final String docFileName;

    /**
     * Containing dir.
     */
    private final String dirPathString;

    private Boolean allowSave = true;

    /**
     * Create new IO and parse the project doc.
     *
     * @param xmlFile doc to parse.
     */
    public Io(final File xmlFile) {
        dirPathString = xmlFile.getAbsoluteFile().getParent() + "/";
        myDoc = parseDocXML(xmlFile);
        docFileName = xmlFile.getName();
    }

    /**
     * Create new IO and parse project doc from InputStream.
     *
     * @param docIs Stream.
     * @param dir Containing dir.
     */
    public Io(final InputStream docIs, final String dir) {
        dirPathString = dir;
        myDoc = parseDocXML(docIs);
        docFileName = "Stream";
        allowSave = false;
    }

    /**
     * Create new IO and parse the custom doc.
     *
     * @param xmlFile File.
     * @param customFactory Custom factory to build the doc.
     */
    public Io(final File xmlFile, final NodeFactory customFactory) {
        dirPathString = xmlFile.getAbsoluteFile().getParent() + "/";
        myDoc = parseGenericXML(xmlFile, customFactory);
        docFileName = xmlFile.getName();
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
     * @param doc document to save.
     * @throws IOException bad file.
     */
    public void saveDoc(final Document doc) throws IOException {
        saveDocAs(doc, dirPathString + docFileName);
    }

    /**
     * Save document to new location.
     *
     * @param doc document to save.
     * @param newPath Path to save to.
     * @throws IOException bad file.
     */
    public void saveDocAs(final Document doc, final String newPath) throws IOException {
        if (!allowSave) {
            throw new IOException("Saving Not Permitted.");
        }
        String path;
        try {
            path = new URI(pathToURI(newPath)).getPath();
        } catch (URISyntaxException ex) {
            throw new IOException("Invalid File Path.");
        }
        Serializer serializer = new Serializer(new FileOutputStream(path), "ISO-8859-1");
        serializer.write(doc);
    }

    /**
     * Get a resource from path.
     *
     * @param path Resource path.
     * @return Optional resource bytes.
     */
    public synchronized Optional<byte[]> getResource(final String path) {
        if (dirPathString == "internal_ui") {
            return getInternalResource(path, MainController.class);
        }

        var uriString = pathToURI(path); //Ensure path is correct URI
        try {
            var uri = new URI(uriString);
            var is = uri.toURL().openStream();
            return Optional.of(is.readAllBytes());
        } catch (URISyntaxException | MalformedURIException | IOException ex) {
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    /**
     * Get an internal resource.
     *
     * @param path resource to get.
     * @param resourceClass Class it is in.
     * @return
     */
    public static Optional<byte[]> getInternalResource(final String path, final Class resourceClass) {
        byte[] bytes = null;
        try {
            bytes = resourceClass.getResourceAsStream(path)
                    .readAllBytes();
        } catch (IOException | NullPointerException ex) {
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.ofNullable(bytes);
    }

    /**
     * Turn a (possibly relative) path into a correct URI.
     *
     * @param path Path to convert.
     * @return Converted path.
     */
    private String pathToURI(final String path) {
        String loc = null;
        if (path.contains(":/") || path.startsWith("/")) {
            //Must be an absolute Path
            loc = path;
        } else if (path.startsWith(".")) {
            //Must be a relative Path
            loc = dirPathString.concat(path);
        }
        if (!path.startsWith("http")) {
            //Not a URL? Must be a local file
            loc = "file:".concat(loc);
        }
        return loc;
    }

    /**
     * Get an XOM compatible parser, which is either validating or
     * non-validating.
     *
     * @param validate Validate or not.
     * @return Parser.
     */
    private XMLReader getParser(final Boolean validate) {
        XMLReader xerces = null;
        try {
            xerces = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            xerces.setFeature("http://apache.org/xml/features/validation/schema", validate);
        } catch (SAXException ex) {
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xerces;
    }

    /**
     * Return the fully parsed representation of the XML doc.
     *
     * @param xmlFile file for XML.
     * @return Optional doc.
     */
    private Optional<Document> parseDocXML(final File xmlFile) {
        try {
            var doc = parseDocXML(new FileInputStream(xmlFile));
            return doc;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Optional.empty();
    }

    /**
     * Return the fully parsed representation of the XML doc.
     *
     * @param xmlStream streamed XML.
     * @return Optional doc.
     */
    private Optional<Document> parseDocXML(final InputStream xmlStream) {
        Builder parser;
        var xer = (XMLReader) getParser(true);
        try {
            var pwsURL = Io.class.getResource(PWS);
            var extURL = Io.class.getResource(EXT_SCHEMA);
            var schemaString = "http://"+PWS_NS + " " + pwsURL.toString() + " " + "http://"+EXT_NS + " " + extURL.toString();
            //Set Schemas
            xer.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaString);
        } catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
            Logger.getLogger(Io.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            parser = new Builder(xer, true, new ElementFactory());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
        Document doc = null;
        try {
            doc = parser.build(xmlStream);
            if (doc != null) {
                var root = doc.getRootElement();
                if (root instanceof DocElement) {
                    ((DocElement) doc.getRootElement()).setBaseDir(dirPathString);
                }
            }
        } catch (ValidityException vex) { //Some sort of Schema failure.
            doc = vex.getDocument();
            var root = doc.getRootElement();
            if (root instanceof DocElement) {
                DocElement docEl = (DocElement) root;
                var errList = new ArrayList<String>();
                for (int i = 0; i < vex.getErrorCount(); i++) {
                    errList.add(vex.getValidityError(i));
                }
                docEl.setValidationErrors(errList);
            }
        } catch (ParsingException | IOException ex) { //We're returning an optional
            ex.printStackTrace();   //So I'm not throwing this out of the method
        }
        return Optional.ofNullable(doc);
    }

    /**
     * Return the fully parsed representation of the XML doc.
     *
     * @param xmlFile File.
     * @param factory Custom factory.
     * @return Optional of doc.
     */
    private Optional<Document> parseGenericXML(final File xmlFile, final NodeFactory factory) {
        Builder parser = (factory == null) ? new Builder(false) : new Builder(factory);
        Document doc = null;
        try {
            doc = parser.build(xmlFile);
        } catch (ParsingException | IOException ex) { //We're returning an optional
            ex.printStackTrace();   //So I'm not throwing this out of the method
        }
        return Optional.ofNullable(doc);
    }

}
