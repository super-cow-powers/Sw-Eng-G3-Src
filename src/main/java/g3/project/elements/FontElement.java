/*
 * Copyright (c) 2022, Group 3
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

import static g3.project.elements.VisualElement.derefAttribute;
import g3.project.graphics.FontProps;
import g3.project.graphics.StyledTextSeg;
import java.util.HashMap;
import java.util.Optional;
import javafx.scene.paint.Color;
import nu.xom.*;

/**
 *
 * @author Group 3
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
        FontProps propsMap = new FontProps();
        for (String prop : FontProps.PROPS_MAP.keySet()) {
            switch (prop) {
                //Special cases
                case (FontProps.ALIGNMENT):
                    break;
                case (FontProps.VALIGNMENT):
                    break;
                default: //Not a special case
                    var attrMaybe = derefAttribute(this, prop);
                    //this.getAttribute(prop, prop)
                    if (attrMaybe.isPresent()) {
                        var attr = attrMaybe.get();
                        var attrVal = attr.getValue();
                        Class attrType = FontProps.PROPS_MAP.get(prop);
                        Object propVal;
                        //Cast to correct type
                        if (attrType == Double.class) {
                            propVal = Double.valueOf(attrVal);
                        } else if (attrType == Boolean.class) {
                            propVal = Boolean.valueOf(attrVal);
                        } else if (attrType == Color.class) {
                            propVal = Color.web(attrVal);
                        } else {
                            propVal = attrVal; //Probably a string.
                        }
                        propsMap.put(prop, propVal);
                    }
                    break;
            }
        }
        return propsMap;
    }

    /**
     * Set this object's properties.
     *
     * @param props Properties.
     */
    public void setProperties(final HashMap<String, Object> props) {
        for (String prop : props.keySet()) {
            var propVal = props.get(prop);
            if (propVal != null) {
                switch (prop) {
                    //Special cases
                    default: //Not a special case
                        var attr = VisualElement.makeAttrWithNS(prop, propVal.toString());
                        this.addAttribute(attr);
                        break;
                }
            }
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
