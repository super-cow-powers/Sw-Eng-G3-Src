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
 * @author David Miall<dm1306@york.ac.uk>
 */
public class ImageElement extends VisualElement implements Includable {

    /**
     * Static method to return builder.
     *
     * @return builder
     */
    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    /**
     * Constructor.
     *
     * @param name Element name.
     */
    public ImageElement(final String name) {
        super(name);
    }

    /**
     * Constructor.
     *
     * @param name Element name.
     * @param uri Element URI.
     */
    public ImageElement(final String name, final String uri) {
        super(name, uri);
    }

    /**
     * Constructor.
     *
     * @param element Element.
     */
    public ImageElement(final Element element) {
        super(element);
    }

    /**
     * Constructor with Source location.
     *
     * @param name Element name.
     * @param uri Element URI.
     * @param sourcePath Source Path.
     */
    public ImageElement(final String name, final String uri, final String sourcePath) {
        super(name, uri);
        var sourceAttr = new Attribute(INCLUDE_ATTR, sourcePath);
        this.addAttribute(sourceAttr);
    }

    /**
     * Get the image's source path or URL.
     *
     * @return Location string.
     */
    @Override
    public final Optional<String> getSourceLoc() {
        //Get include_source attribute
        return Optional.ofNullable(this.getAttribute(INCLUDE_ATTR))
                .map(f -> f.getValue());

    }

}
