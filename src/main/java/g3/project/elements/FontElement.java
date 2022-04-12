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

import g3.project.graphics.FontProps;
import java.util.Optional;
import javafx.scene.paint.Color;
import nu.xom.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class FontElement extends Element {
//CHECKSTYLE:OFF

    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    public FontElement(final String name) {
        super(name);
    }

    public FontElement(final String name, String uri) {
        super(name, uri);
    }

    public FontElement(final Element element) {
        super(element);
    }
    
    public FontElement(final String name, String uri, String textString) {
        super(name, uri);
        this.insertChild(textString, 0); //Add text
    }
//CHECKSTYLE:ON

    /**
     * Get the element's font styling properties.
     *
     * @return FontProps properties.
     */
    public FontProps getProperties() {
        final double defSize = 10d;
        var usA = this.getAttribute("underscore");
        var bldA = this.getAttribute("bold");
        var itA = this.getAttribute("italic");
        var sizeA = this.getAttribute("size");
        var nameA = this.getAttribute("name");
        var colOpt = this.getCol();
        Color col = Color.BLACK;
        boolean us = false;
        boolean bld = false;
        boolean it = false;
        var size = defSize;
        String name = "";

        if (usA != null) {
            us = Boolean.parseBoolean(usA.getValue());
        }
        if (bldA != null) {
            bld = Boolean.getBoolean(bldA.getValue());
        }
        if (itA != null) {
            it = Boolean.getBoolean(itA.getValue());
        }
        if (sizeA != null) {
            size = Double.valueOf(sizeA.getValue());
        }
        if (nameA != null) {
            name = nameA.getValue();
        }
        if (colOpt.isPresent()) {
            col = colOpt.get();
        }
        return new FontProps(us, it, bld, size, name, col);
    }

    /**
     * Get font colour.
     *
     * @return Optional font colour.
     */
    private Optional<Color> getCol() {
        final int lenRGB = 6;
        final int lenRGBA = 8;
        var colA = Optional.ofNullable(this.getAttribute("colour"));
        /**
         * @todo: Find a nicer looking way of making this work Probably
         * containing more streams
         */
        if (colA.isPresent()) {
            var colStr = colA.get().getValue().replace("#", "");

            switch (colStr.length()) {
                case lenRGB:
                    //CHECKSTYLE:OFF
                    return Optional.of(new Color(
                            (double) Integer.valueOf(colStr.substring(0, 2), 16) / 255,
                            (double) Integer.valueOf(colStr.substring(2, 4), 16) / 255,
                            (double) Integer.valueOf(colStr.substring(4, 6), 16) / 255,
                            1.0d));
                    //CHECKSTYLE:ON
                case lenRGBA:
                    //CHECKSTYLE:OFF
                    return Optional.of(new Color(
                            (double) Integer.valueOf(colStr.substring(0, 2), 16) / 255,
                            (double) Integer.valueOf(colStr.substring(2, 4), 16) / 255,
                            (double) Integer.valueOf(colStr.substring(4, 6), 16) / 255,
                            (double) Integer.valueOf(colStr.substring(6, 8), 16) / 255));
                    //CHECKSTYLE:ON
                default:
                    return Optional.empty();

            }
        } else { //No colour specified
            return Optional.empty();
        }
    }
}
