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

import java.util.Optional;
import nu.xom.*;

/**
 *
 * @author David Miall <dm1306@york.ac.uk>
 */
public class PageElement extends VisualElement {
    /**
     * My Index.
     */
    private Integer index = 0;

    /**
     * Creates builder thread for the element
     */
    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    /**
     * Constructor
     * @param name
     */
    public PageElement(final String name) {
        super(name);
    }

    /**
     * Constructor
     * @param name
     * @param uri
     */
    public PageElement(final String name, final String uri) {
        super(name, uri);
    }

    /**
     * Constructor
     * @param element
     */
    public PageElement(final Element element) {
        super(element);
    }

    /**
     * @TODO fill out javadoc
     * @return
     */
    public Optional<String> getTitle() {
        var title = this.getAttribute("title");
        return (title != null) ? Optional.of(title.getValue()) : Optional.empty();
    }

    /**
     * @TODO fill out javadoc
     * @param name
     * @return
     */
    public Optional<String> setTitle(final String name) {
        this.addAttribute(new Attribute("title", name));
        return getTitle();
    }
    /**
     * Set the page index.
     * This should be done before return from the Document to a user.
     * @param ind Index.
     */
    protected void setIndex(final Integer ind){
        index = ind;
    }
    /**
     * Get the page index. This should be set before use in the engine.
     * @return Index.
     */
    public Integer getIndex(){
        return index;
    }

}
