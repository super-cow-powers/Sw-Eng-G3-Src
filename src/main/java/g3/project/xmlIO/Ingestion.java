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

import g3.project.elements.ElementFactory;
import java.util.Optional;
import java.io.File;
import java.io.IOException;
import nu.xom.*;

/**
 *
 * @author david
 */
public class Ingestion {

    /* Return new Ingestion class */
    public Ingestion() {

    }

    /* Return the fully parsed representation of the XML doc */
    public Optional<Document> parseDocXML(File xmlFile){
        Builder parser = new Builder(new ElementFactory());
        Document doc = null;
        try {
            doc = parser.build(xmlFile);
        } catch (ParsingException ex) {//We're returning an optional
            ex.printStackTrace();   //So I'm not throwing this out of the method
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Optional.ofNullable(doc);
    }
    public Optional<Document> parseDocXML(String xmlDocString){
        Builder parser = new Builder(new ElementFactory());
        Document doc = null;
        try {
            doc = parser.build(xmlDocString, null); //No base URL
        } catch (ParsingException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Optional.ofNullable(doc);
    }
    
    /* Parse a generic doc */
    public Optional<Document> parseGenericXML(String xmlDocString, NodeFactory factory){
        Builder parser = (factory == null)? new Builder(false): new Builder(factory);
        Document doc = null;
        try {
            doc = parser.build(xmlDocString, null); //No base URL
        } catch (ParsingException ex) { 
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Optional.ofNullable(doc);
    }
    /* Return the fully parsed representation of the XML doc */
    public Optional<Document> parseGenericXML(File xmlFile, NodeFactory factory){
        Builder parser = (factory == null)? new Builder(false): new Builder(factory);
        Document doc = null;
        try {
            doc = parser.build(xmlFile);
        } catch (ParsingException ex) {//We're returning an optional
            ex.printStackTrace();   //So I'm not throwing this out of the method
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Optional.ofNullable(doc);
    }

}
