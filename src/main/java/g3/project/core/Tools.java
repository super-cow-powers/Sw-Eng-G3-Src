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

import nu.xom.Builder;
import nu.xom.Element;

import java.util.ArrayList;
import java.util.Optional;

/**
<<<<<<< Updated upstream
 *
 * @author David Miall<dm1306@york.ac.uk>
=======
 * @author Group 3
>>>>>>> Stashed changes
 */
public class Tools extends Element {

    /**
     * Create builder.
     */
    private static final ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ToolsFactory());
        }

    };

    /**
     * Constructor.
     *
     * @param name Tools element name.
     */
    public Tools(final String name) {
        super(name);
    }

    /**
     * Constructor.
     *
     * @param name Tools element name.
     * @param uri  Tools element URI.
     */
    public Tools(final String name, final String uri) {
        super(name, uri);
    }

    /**
     * Constructor.
     *
     * @param element Tools Element.
     */
    public Tools(final Element element) {
        super(element);
    }

    /**
     * Get list of available tools.
     *
     * @return ArrayList of tools.
     */
    public final ArrayList<Tool> getTools() {
        ArrayList<Tool> list = new ArrayList<Tool>();
        for (int i = 0; i < this.getChildCount(); i++) {
            var child = this.getChild(i);
            if (child instanceof Tool) {
                list.add((Tool) child);
            }
        }
        return list;
    }

    /**
     * Get a tool by it's ID.
     *
     * @param toolID Target ID.
     * @return Maybe tool.
     */
    public Optional<Tool> getTool(final String toolID) {
        var children = this.getChildElements();
        for (var ch : children) {
            if (ch instanceof Tool) {
                if (((Tool) ch).getID().equals(toolID)) {
                    return Optional.of((Tool) ch);
                }
            }
        }
        return Optional.empty();
    }
}
