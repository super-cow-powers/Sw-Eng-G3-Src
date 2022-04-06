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
package g3.project.core;

import g3.project.elements.ScriptElement;
import nu.xom.Builder;
import nu.xom.Element;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class Tool extends Element {

    /**
     * Create a builder.
     */
    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ToolsFactory());
        }

    };

    /**
     * Constructor.
     *
     * @param name Tool name.
     */
    public Tool(final String name) {
        super(name);
    }

    /**
     * Constructor.
     *
     * @param name Tool name.
     * @param uri Tool URI.
     */
    public Tool(final String name, final String uri) {
        super(name, uri);
    }

    /**
     * Constructor.
     *
     * @param element Tool Element.
     */
    public Tool(final Element element) {
        super(element);
    }

    /**
     * Get tool name.
     *
     * @return Tool name string.
     */
    public final String getName() {
        var name = this.getAttribute("name");
        return name.getValue();
    }

    /**
     * Get tool ID.
     *
     * @return Tool ID string.
     */
    public final String getID() {
        var id = this.getAttribute("ID");
        return id != null ? id.getValue() : "tool-null-id";
    }

    /**
     * Get tool script string.
     *
     * @return script string.
     */
    public final String getScriptString() {
        var el = this.getChildElements("script").get(0);
        return "";
        //return (el instanceof ScriptElement) ? ((ScriptElement) el).getInlineScriptString() : "";
    }
}
