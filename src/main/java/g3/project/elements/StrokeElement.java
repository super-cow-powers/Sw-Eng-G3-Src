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

import g3.project.graphics.StrokeProps;
import java.util.Optional;
import javafx.scene.paint.Color;
import nu.xom.*;

/**
 *
 * @author Group 3
 */
public class StrokeElement extends Element {

    /**
     * Creates builder thread for the element.
     */
    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    /**
     * Constructor.
     *
     * @param name Element Name.
     */
    public StrokeElement(final String name) {
        super(name);
    }

    /**
     * Constructor
     *
     * @param name Element Name.
     * @param uri Element URI.
     */
    public StrokeElement(final String name, final String uri) {
        super(name, uri);
    }

    /**
     * Constructor.
     *
     * @param element Element.
     */
    public StrokeElement(final Element element) {
        super(element);
    }

    /**
     * Get the stroke's colour.
     *
     * @return Maybe colour.
     */
    public final Optional<Color> getColour() {
        var col = Optional.ofNullable(this.getAttribute(StrokeProps.COLOUR));
        // @todo: Find a nicer looking way of making this work Probably
        // containing more streams.
        if (col.isPresent()) {
            //var colStr = col.get().getValue().replace("#", "");
            var colStr = col.get().getValue();
            return Optional.ofNullable(Color.web(colStr));
        }

        return Optional.empty();
    }

    /**
     * Set colour of Stroke (RGBA).
     *
     * @param colourString RGBA colour string.
     */
    public final void setColour(final String colourString) {
        var colAttr = new Attribute(StrokeProps.COLOUR, colourString);
        this.addAttribute(colAttr);
    }

    /**
     * Get the stroke's width.
     *
     * @return Optional width.
     */
    public final Optional<Double> getWidth() {
        var width = Optional.ofNullable(this.getAttribute(StrokeProps.WIDTH));
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
        var widthAttr = new Attribute(StrokeProps.WIDTH, width.toString());
        this.addAttribute(widthAttr);
    }

    /**
     * Get the dash style.
     *
     * @return Optional dash style.
     */
    public final Optional<String> getStyle() {
        var style = Optional.ofNullable(this.getAttribute(StrokeProps.DASH_STYLE));
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
    public final void setStyle(final String style) {
        var styleAttr = new Attribute(StrokeProps.DASH_STYLE, style);
        this.addAttribute(styleAttr);
    }

    /**
     * Get stroke properties.
     *
     * @return StrokeProps.
     */
    public final StrokeProps getProps() {
        StrokeProps props = new StrokeProps();
        for (String prop : props.getPropsTypes().keySet()) {
            var attr = this.getAttribute(prop);
            if (attr != null) {
                var attrVal = attr.getValue();
                Class attrType = props.getPropsTypes().get(prop);
                Object propVal;
                if (attrType == Double.class) {
                    propVal = Double.valueOf(attrVal);
                } else if (attrType == Boolean.class) {
                    propVal = Boolean.valueOf(attrVal);
                } else if (attrType == Color.class) {
                    propVal = Color.web(attrVal);
                } else {
                    propVal = attrVal; //Probably a string.
                }
                props.put(prop, propVal);
            }
        }
        return props;
    }

    /**
     * Set stroke props/attributes.
     *
     * @param props Stroke Properties.
     */
    public final void setProps(final StrokeProps props) {
        for (String prop : props.keySet()) {
            Attribute newAttr = new Attribute(prop, props.get(prop).toString());
        }
    }
}
