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
import g3.project.graphics.LocObj;
import g3.project.graphics.StrokeProps;
import g3.project.graphics.StyledTextSeg;
import g3.project.xmlIO.DocIO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import nu.xom.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class ShapeElement extends VisualElement {

    private final int MIN_POINTS = 4;

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
     *
     * @param name
     */
    public ShapeElement(final String name) {
        super(name);
    }

    /**
     * Constructor
     *
     * @param name
     * @param uri
     */
    public ShapeElement(final String name, final String uri) {
        super(name, uri);
    }

    /**
     * Constructor
     *
     * @param element
     */
    public ShapeElement(final Element element) {
        super(element);
    }

    public String getType() {
        var type = this.getAttribute("type");
        return type != null ? type.getValue() : "";
    }

    public String setType(String type) {
        this.addAttribute(new Attribute("type", type));
        return this.getType();
    }

    @Override
    public void delete(final DocIO resIO) {
        for (var ch : this.getChildElements()) {
            this.removeChild(ch);
        }
        this.detach();
    }

    /**
     * Set a shape's stroke.
     *
     * @param colour Colour to set.
     * @param style Stroke Style.
     * @param width Stroke Width.
     */
    public void setStroke(final String colour, final String style, final Double width) {
        for (Element el : this.getChildElements()) {
            if (el instanceof StrokeElement) {
                this.removeChild(el);
            }
        }
        var stroke = new StrokeElement("base:stroke", BASE_URI);
        stroke.setWidth(width);
        stroke.setStyle(style);
        stroke.setColour(colour);
        this.appendChild(stroke);
    }

    /**
     * Set segment points for polygon or alt-line.
     *
     * @param points Segment points.
     * @throws java.lang.Exception
     */
    public final void setSegPoints(final ArrayList<Double> points) throws Exception {
        if (points.size() % 2 > 0) {
            throw new Exception("Points list must have even length!");
        }
        for (Integer i = 0; i < points.size(); i++) {
            var segEl = new Element("polyseg", this.EXT_URI);
            Attribute segAttr;
            if (i % 2 == 0) {
                segAttr = new Attribute("x_start", EXT_URI, String.valueOf(points.get(i)));
            } else {
                segAttr = new Attribute("y_start", EXT_URI, String.valueOf(points.get(i)));
            }
            segEl.addAttribute(segAttr);
            this.appendChild(segEl);
        }
        if (this.getType() == "line") { //I'm a line. Also define old attributes for compatibility.
            if (points.size() < MIN_POINTS) {
                throw new Exception("Too few points!");
            }
            this.removeAttribute(this.getAttribute("x_end"));
            this.removeAttribute(this.getAttribute("y_end"));
            Attribute segAttr = new Attribute("x_end", String.valueOf(points.get(2)));
            this.addAttribute(segAttr);
            segAttr = new Attribute("y_end", String.valueOf(points.get(3)));
            this.addAttribute(segAttr);
            var origin = this.getOrigin();
            origin.ifPresentOrElse(o -> {
                var newOrig = new LocObj(new Point2D(points.get(0), points.get(1)), o.getZ());
                this.setOriginXY(newOrig);
            },
                    () -> {
                        var newOrig = new LocObj(new Point2D(points.get(0), points.get(1)), 1d);
                        this.setOriginXY(newOrig);
                    });
        }

    }

    /**
     * Get a list of points if the element is a line or polygon.
     *
     * @return List of points if line or polygon, else returns an empty list.
     */
    public final ArrayList<Double> getSegPoints() {
        var points = new ArrayList<Double>();
        for (var ch : this.getChildElements()) {
            if (ch.getLocalName().toLowerCase().equals("polyseg")) {
                var xAttr = ch.getAttribute("x");
                var yAttr = ch.getAttribute("y");
                if (!(xAttr == null || yAttr == null)) {
                    points.add(Double.valueOf(xAttr.getValue()));
                    points.add(Double.valueOf(yAttr.getValue()));
                }
            }
        }
        if (this.getType().equals("line")) { //I'm a line. Check for old line attributes.
            if (points.size() == 0) {
                var xAttr = this.getAttribute("x_end");
                var yAttr = this.getAttribute("y_end");
                var maybeStart = this.getOrigin();
                if (!(xAttr == null || yAttr == null || maybeStart.isEmpty())) {
                    var start = maybeStart.get();
                    points.add(start.getLoc().getX());
                    points.add(start.getLoc().getY());
                    points.add(Double.valueOf(xAttr.getValue()));
                    points.add(Double.valueOf(yAttr.getValue()));
                }

            }
        }
        return points;
    }

    /**
     * Gets text for shape element - optional as wont necessarily exist
     *
     * @return arraylist of text in the object
     */
    public Optional<ArrayList<StyledTextSeg>> getText() {
        ArrayList<StyledTextSeg> text = null;
        for (var ch : this.getChildElements()) {
            if (ch instanceof TextElement) {
                text = ((TextElement) ch).getText();
            }
        }
        return Optional.ofNullable(text);
    }

    /**
     * Get my text string.
     *
     * @return String of text contained.
     */
    public String getTextString() {
        String retString = "";
        ArrayList<StyledTextSeg> text = null;
        for (var ch : this.getChildElements()) {
            if (ch instanceof TextElement) {
                text = ((TextElement) ch).getText();
            }
        }
        if (text != null) {
            for (var s : text) {
                retString = retString.concat(s.getString());
            }
        }
        return retString;
    }

    /**
     * Sets text of the shape element.
     *
     * @param text Text segments to set.
     */
    public final void setText(final ArrayList<StyledTextSeg> text) {
        for (var ch : this.getChildElements()) {
            if (ch instanceof TextElement) {
                this.removeChild(ch);
                ch.detach();
            }
        }
        var textEl = new TextElement("base:text", BASE_URI, text);
        this.appendChild(textEl);
    }

    /**
     * Set a text string, preserving properties of the first segment.
     *
     * @param text Text string to set.
     */
    public void setText(final String text) {
        var maybeText = this.getText();
        var maybeStyle = maybeText.map(t -> t.get(0)).map(s -> s.getStyle());
        FontProps props;
        if (maybeStyle.isPresent()) {
            props = maybeStyle.get();
        } else {
            props = new FontProps();
        }
        var segArr = new ArrayList<StyledTextSeg>();
        var seg = new StyledTextSeg(props, text);
        segArr.add(seg);
        this.setText(segArr);
        this.setTextProperties(props);
    }

    /**
     * Set my text properties via map.
     *
     * @param props Properties to set.
     */
    public void setTextProperties(final HashMap<String, Object> props) {
        Optional<TextElement> text = Optional.empty();
        for (var ch : this.getChildElements()) {
            if (ch instanceof TextElement) {
                text = Optional.of(((TextElement) ch));
            }
        }
        text.map(te -> {
            var myAlign = props.get(FontProps.ALIGNMENT);
            var myVAlign = props.get(FontProps.VALIGNMENT);
            setAlignment(myAlign != null ? myAlign.toString() : "", myVAlign != null ? myVAlign.toString() : "");

            for (var ce : te.getChildElements()) {
                if (ce instanceof FontElement) {
                    return ce;
                }
            }
            return null;
        }).ifPresent(fe -> ((FontElement) fe).setProperties(props));
    }

    /**
     * Set alpha of contained text.
     *
     * @param alpha Opacity/Alpha value.
     */
    public void setTextAlpha(final Double alpha) {
        Optional<TextElement> text = Optional.empty();
        for (var ch : this.getChildElements()) {
            if (ch instanceof TextElement) {
                text = Optional.of(((TextElement) ch));
            }
        }
        text.map(t -> {
            var chArr = new ArrayList<FontElement>();
            for (var ce : t.getChildElements()) {
                if (ce instanceof FontElement) {
                    chArr.add((FontElement) ce);
                }
            }
            return chArr.isEmpty() ? null : chArr;
        }).ifPresent(feArr -> {
            feArr.forEach(fe -> {
                var props = fe.getProperties();
                Color oldCol = (Color) props.getProp(FontProps.COLOUR).get();
                Color newCol = new Color(oldCol.getRed(), oldCol.getGreen(), oldCol.getBlue(), alpha);
                props.put(FontProps.COLOUR, newCol.toString());
                fe.setProperties(props);
            });
        });
    }

    /**
     * Set text Colour.
     *
     * @param colour Colour String.
     */
    public void setTextColour(final String colour) {
        Optional<TextElement> text = Optional.empty();
        for (var ch : this.getChildElements()) {
            if (ch instanceof TextElement) {
                text = Optional.of(((TextElement) ch));
            }
        }
        text.map(t -> {
            var chArr = new ArrayList<FontElement>();
            for (var ce : t.getChildElements()) {
                if (ce instanceof FontElement) {
                    chArr.add((FontElement) ce);
                }
            }
            return chArr.isEmpty() ? null : chArr;
        }).ifPresent(feArr -> {
            feArr.forEach(fe -> {
                var props = fe.getProperties();
                Color newCol = Color.valueOf(colour);
                props.put(FontProps.COLOUR, newCol.toString());
                fe.setProperties(props);
            });
        });
    }

    /**
     * Get Text Colour.
     *
     * @return Text Colour.
     */
    public Optional<Color> getTextColour() {
        Optional<TextElement> text = Optional.empty();
        for (var ch : this.getChildElements()) {
            if (ch instanceof TextElement) {
                text = Optional.of(((TextElement) ch));
            }
        }
        return text.map(t -> {
            for (var ce : t.getChildElements()) {
                if (ce instanceof FontElement) {
                    return (Color)((FontElement) ce).getProperties().getProp(FontProps.COLOUR).get();
                }
            }
            return null;
        });
    }

    /**
     * Set text alignment.
     *
     * @param hAlign Horizontal.
     * @param vAlign Vertical.
     */
    public void setAlignment(final String hAlign, final String vAlign) {
        Optional<TextElement> text = Optional.empty();
        for (var ch : this.getChildElements()) {
            if (ch instanceof TextElement) {
                text = Optional.of(((TextElement) ch));
            }
        }
        text.ifPresent(t -> {
            if (!hAlign.isBlank()) {
                t.addAttribute(VisualElement.makeAttrWithNS(FontProps.ALIGNMENT, hAlign));
            }
            if (!vAlign.isBlank()) {
                t.addAttribute(VisualElement.makeAttrWithNS(FontProps.VALIGNMENT, vAlign));
            }
        });
    }

    /**
     * Set my font.
     *
     * @param font Font Name.
     * @param colour Font Colour.
     * @param size Font Size.
     * @param underscore Underscore?
     * @param italic Italicise?
     * @param bold Bold?
     */
    public void setFont(final String font, final String colour, final Double size, final Boolean underscore, final Boolean italic, final Boolean bold) {
        var maybeText = this.getText();
        maybeText.map(t -> t.get(0)).ifPresent(s -> {
            var props = s.getStyle();
            props.put(FontProps.COLOUR, Color.valueOf(colour));
            props.put(FontProps.FONT, font);
            props.put(FontProps.SIZE, size);
            props.put(FontProps.US, underscore);
            props.put(FontProps.IT, italic);
            props.put(FontProps.BOLD, bold);
        });
    }

}
