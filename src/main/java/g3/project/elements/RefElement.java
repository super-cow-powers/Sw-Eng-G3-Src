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
import g3.project.graphics.StyledTextSeg.REF_TYPE;
import java.util.ArrayList;
import java.util.Optional;
import nu.xom.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class RefElement extends Element {
//CHECKSTYLE:OFF

    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    public RefElement(String name) {
        super(name);
    }

    public RefElement(String name, String uri) {
        super(name, uri);
    }

    public RefElement(Element element) {
        super(element);
    }

    public RefElement(String name, String uri, String target, REF_TYPE refType) {
        super(name, uri);
        var refTypeName = refType == REF_TYPE.INTERNAL ? "internal" : "external";
        var typeAttr = new Attribute("type", refTypeName);
        this.addAttribute(typeAttr); //Add ref type
        var tgtAttr = new Attribute("target", target);
        this.addAttribute(tgtAttr); //Add ref target
    }

//CHECKSTYLE:ON
    /**
     * Get link text.
     *
     * @return String.
     */
    public final String getText() {
        return this.getValue();
    }

    /**
     * Get the ref's type. Will guess if not specified.
     *
     * @return ref type. Internal or External.
     */
    public final REF_TYPE getType() {
        var typeAttr = this.getAttribute("type");
        if (typeAttr != null) {
            var type = typeAttr.getValue();
            if (type == "external") {
                return REF_TYPE.EXTERNAL;
            } else {
                return REF_TYPE.INTERNAL;
            }
        } else { //Try to guess.
            System.err.println("No ref type specified. I will have to guess!");
            var tgt = this.getTarget();
            if (tgt != null) {
                //Just assume an ID wouldn't have ':/' in it.
                if (tgt.contains(":/")) {
                    return REF_TYPE.EXTERNAL;
                } else {
                    return REF_TYPE.INTERNAL;
                }
            } else {
                //We're really screwed now. There's no target.
                //Just point it at something internal and have done.
                return REF_TYPE.INTERNAL;
            }
        }
    }

    /**
     * Get ref target. RETURNS NULL IF NO TARGET GIVEN! Should never return null
     * in practice.
     *
     * @return Ref Target.
     */
    public final String getTarget() {
        var tgtAttr = this.getAttribute("target");
        return tgtAttr == null ? null : tgtAttr.getValue();
    }
}
