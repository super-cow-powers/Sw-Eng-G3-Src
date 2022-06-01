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

import g3.project.graphics.LocObj;
import g3.project.graphics.StrokeProps;
import g3.project.graphics.StyledTextSeg;
import g3.project.xmlIO.DocIO;
import java.util.ArrayList;
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
     * Sets text of the shape element
     *
     * @param text
     */
    public final void setText(ArrayList<StyledTextSeg> text) {
        for (var ch : this.getChildElements()) {
            if (ch instanceof TextElement) {
                this.removeChild(ch);
                ch.detach();
            }
        }
        var textEl = new TextElement("base:text", BASE_URI, text);
        this.appendChild(textEl);
        hasUpdated();
    }

}
