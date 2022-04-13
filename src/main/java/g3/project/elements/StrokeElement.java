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
import javafx.scene.paint.Color;
import nu.xom.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class StrokeElement extends Element {

    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    public StrokeElement(String name) {
        super(name);
    }

    public StrokeElement(String name, String uri) {
        super(name, uri);
    }

    public StrokeElement(Element element) {
        super(element);
    }

    /**
     * Get the stroke's colour.
     *
     * @return Optional colour.
     */
    public final Optional<Color> getColour() {
        final int lenRGB = 6;
        final int lenRGBA = 8;
        var col = Optional.ofNullable(this.getAttribute("colour"));
        /**
         * @todo: Find a nicer looking way of making this work Probably
         * containing more streams.
         */
        if (col.isPresent()) {
            var colStr = col.get().getValue().replace("#", "");

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
            }
        }

        return Optional.empty();
    }

    /**
     * Set colour of Stroke (RGBA).
     *
     * @param colourString RGBA colour string.
     */
    public final void setColour(final String colourString) {
        var colAttr = new Attribute("colour", colourString);
        this.addAttribute(colAttr);
    }

    /**
     * Get the stroke's width.
     *
     * @return Optional width.
     */
    public final Optional<Double> getWidth() {
        var width = Optional.ofNullable(this.getAttribute("width"));
        if (width.isPresent()) {
            return Optional.of(Double.valueOf(width.get().getValue()));
        }
        return Optional.empty();
    }

    /**
     * Set the Stroke width.
     *
     * @param width Width.
     */
    public final void setWidth(final Double width) {
        var widthAttr = new Attribute("width", width.toString());
        this.addAttribute(widthAttr);
    }

    /**
     * Get the dash style.
     *
     * @return Optional dash style.
     */
    public final Optional<String> getStyle() {
        var style = Optional.ofNullable(this.getAttribute("dash-style"));
        if (style.isPresent()) {
            return Optional.of(style.get().getValue());
        }
        return Optional.empty();
    }

    /**
     * Set the Stroke dash style.
     *
     * @param style Style.
     */
    public final void setWidth(final String style) {
        var styleAttr = new Attribute("dash-style", style);
        this.addAttribute(styleAttr);
    }

}
