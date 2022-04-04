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

import nu.xom.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class ElementFactory extends NodeFactory {

    @Override
    public Element startMakingElement(final String name, final String namespaceURI) {
        switch (name.toLowerCase()) {
            case "base:document":
                return new DocElement(name, namespaceURI);
            case "base:page":
                return new PageElement(name, namespaceURI);
            case "base:shape":
                return new ShapeElement(name, namespaceURI);
            case "base:image":
                return new ImageElement(name, namespaceURI);
            case "base:playable":
                return new PlayableElement(name, namespaceURI);
            case "base:table":
                return new TableElement(name, namespaceURI);
            case "base:text":
                return new TextElement(name, namespaceURI);
            case "base:stroke":
                return new StrokeElement(name, namespaceURI);
            case "base:font":
                return new FontElement(name, namespaceURI);
            case "ext:script":
                return new ScriptElement(name, namespaceURI);
            case "script":
                return new ScriptElement(name, namespaceURI);
            default:
                return new Element(name, namespaceURI);
        }

    }
}
