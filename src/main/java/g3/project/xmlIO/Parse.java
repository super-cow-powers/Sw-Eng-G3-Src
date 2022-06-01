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

import g3.project.core.ToolsFactory;
import g3.project.elements.DocElement;
import g3.project.elements.ElementFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.NodeFactory;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class Parse {

    /**
     * PWS file name
     */
    private static final String PWS = "PWS.xsd";

    /**
     * @TODO javadoc
     */
    public static final String PWS_NS = "PWS_Base";

    /**
     * Schema for our extension
     */
    private static final String EXT_SCHEMA = "my_exts.xsd";

    /**
     * PWS extension schema
     */
    public static final String EXT_NS = "PWS_Exts";

    /**
     * Get an XOM compatible parser, which is either validating or
     * non-validating.
     *
     * @param validate Validate or not.
     * @return Parser.
     */
    private static XMLReader getParser(final Boolean validate) {
        XMLReader xerces = null;
        try {
            xerces = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            xerces.setFeature("http://apache.org/xml/features/validation/schema", validate);
        } catch (SAXException ex) {
            Logger.getLogger(DocIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xerces;
    }

    /**
     * Return the fully parsed representation of the XML doc.
     *
     * @param xmlFile file for XML.
     * @return Optional doc.
     */
    public static Optional<Document> parseDocXML(final File xmlFile) {
        try {
            var doc = parseDocXML(new FileInputStream(xmlFile));
            return doc;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DocIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

    /**
     * Return the fully parsed representation of the XML doc.
     *
     * @param xmlStream streamed XML.
     * @return Optional doc.
     */
    public static Optional<Document> parseDocXML(final InputStream xmlStream) {
        Builder parser;
        var xer = (XMLReader) getParser(true);
        try {
            var pwsURL = DocIO.class.getResource(PWS);
            var extURL = DocIO.class.getResource(EXT_SCHEMA);
            var schemaString = "http://" + PWS_NS + " " + pwsURL.toString() + " " + "http://" + EXT_NS + " " + extURL.toString();
            //Set Schemas
            xer.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaString);
        } catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
            Logger.getLogger(DocIO.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (ValidityException vex) { //Some sort of Schema validity failure.
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
            return Optional.empty();
        }
        return Optional.ofNullable(doc);
    }

    /**
     * Return the fully parsed representation of the XML doc.
     *
     * @param xmlStream XML Doc stream.
     * @return Optional of doc.
     */
    public static Optional<Document> parseToolXML(final InputStream xmlStream) {
        Builder parser = new Builder(false, new ToolsFactory());
        Document doc = null;
        try {
            doc = parser.build(xmlStream);
        } catch (ParsingException | IOException ex) { //We're returning an optional
            ex.printStackTrace();   //So I'm not throwing this out of the method
            return Optional.empty();
        }
        return Optional.ofNullable(doc);
    }

}
