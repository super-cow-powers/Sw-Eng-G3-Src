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
import g3.project.graphics.StyledTextSeg;
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

    public FontElement(final String name, String uri, StyledTextSeg textSeg) {
        super(name, uri);
        this.insertChild(textSeg.getString(), 0); //Add text
        this.setProperties(textSeg.getStyle()); //Set the style props.

    }
//CHECKSTYLE:ON

    /**
     * Get the element's font styling properties.
     *
     * @return FontProps properties.
     */
    public FontProps getProperties() {
        FontProps myProps = new FontProps();
        //Find all extant valid props.
        for (String propKey : FontProps.PROPS_MAP.keySet()) {
            Class propClass = FontProps.PROPS_MAP.get(propKey);
            var attr = this.getAttribute(propKey);
            if (attr != null) { //Does the attr exist?
                Object attrVal;
                String attrStr = attr.getValue();
                //Get props as right type.
                if (propClass == Boolean.class) {
                    attrVal = Boolean.parseBoolean(attrStr);
                } else if (propClass == Double.class) {
                    attrVal = Double.valueOf(attrStr);
                } else {
                    attrVal = attrStr;
                }
                myProps.put(propKey, attrVal);
            }
        }
        return myProps;
    }

    /**
     * Set the font properties.
     *
     * @param props Properties to set.
     */
    private void setProperties(final FontProps props) {
        for (String propKey : props.keySet()) {
            var attr = new Attribute(propKey, props.get(propKey).toString());
            this.addAttribute(attr);
        }
    }

    /**
     * Get font colour.
     *
     * @param colString Colour string to convert.
     * @return Optional font colour.
     */
    public static Optional<Color> convCol(final String colString) {
        final int lenRGB = 6;
        final int lenRGBA = 8;

        var colStr = colString.replace("#", "");
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

    }
}
