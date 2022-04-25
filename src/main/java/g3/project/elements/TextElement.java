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
package g3.project.elements;

import g3.project.graphics.StyledTextSeg;
import java.util.ArrayList;
import java.util.Optional;
import nu.xom.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class TextElement extends Element implements Includable {
//CHECKSTYLE:OFF

    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    public TextElement(String name) {
        super(name);
    }

    public TextElement(String name, String uri) {
        super(name, uri);
    }

    public TextElement(Element element) {
        super(element);
    }

    public TextElement(String name, String uri, ArrayList<StyledTextSeg> textSegs) {
        super(name, uri);
        for (StyledTextSeg seg : textSegs) {
            var fontBlock = new FontElement("base:font", uri, seg);
            this.appendChild(fontBlock);
        }
    }
//CHECKSTYLE:ON

    /**
     * Get all font blocks in this text section.
     *
     * @return ArrayList of styled text segments.
     */
    public final ArrayList<StyledTextSeg> getText() {
        var list = new ArrayList<StyledTextSeg>();
        for (var ch : this.getChildElements()) {
            if (ch instanceof FontElement) {
                FontElement chf = (FontElement) ch;
                var props = chf.getProperties();
                //Go through the children to find any links
                for (int i = 0; i < chf.getChildCount(); i++) {
                    var textOrRef = chf.getChild(i);
                    if (textOrRef instanceof nu.xom.Text) {
                        var seg = new StyledTextSeg(props, textOrRef.getValue());
                        list.add(seg);
                    } else if (textOrRef instanceof RefElement) {
                        var seg = new StyledTextSeg(props, textOrRef.getValue());
                        seg.SetHRef(((RefElement) textOrRef).getTarget(), ((RefElement) textOrRef).getType());
                        list.add(seg);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public Optional<String> getSourceLoc() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
