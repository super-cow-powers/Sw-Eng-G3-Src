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

import g3.project.core.RecursiveBindings;
import g3.project.ui.LocObj;
import g3.project.ui.SizeObj;
import java.util.Optional;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class VisualElement extends Element implements Scriptable{

    /**
     * Script bindings for the element.
     */
    private RecursiveBindings elementScriptBindings = new RecursiveBindings();
    /**
     * Clone of this object. Might not use it.
     */
    private VisualElement scriptedClone;

    /**
     * Constructor.
     *
     * @param name Element name
     */
    public VisualElement(final String name) {
        super(name);
    }

    /**
     * Constructor.
     *
     * @param name Element name
     * @param uri Element URI
     */
    public VisualElement(final String name, final String uri) {
        super(name, uri);
    }

    /**
     * Constructor.
     *
     * @param element Element
     */
    public VisualElement(final Element element) {
        super(element);
    }

    /**
     * Get the object's X/Y location. Returns an Optional, which may contain
     * either the location or nothing. The caller can then determine the action
     * to take.
     *
     * @return Optional Location
     */
    public final Optional<LocObj> getLoc() {
        var x = Optional.ofNullable(this.getAttribute("x_orig"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));
        var y = Optional.ofNullable(this.getAttribute("y_orig"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));

        return (x.isPresent() && y.isPresent())
                ? Optional.of(new LocObj(new Point2D(x.get(), y.get()), null, null, getZInd())) : Optional.empty();
    }

    /**
     * Set the object's X/Y location. Returns the new location.
     *
     * @param loc Location to set
     * @return Optional Set Location
     */
    public final Optional<LocObj> setLoc(final LocObj loc) {
        var start = loc.getStart();
        var centre = loc.getCentre();
        var end = loc.getEnd();

        /**
         * @todo Assign centre and end.
         */
        start.ifPresent(s -> {
            this.addAttribute(new Attribute("x_orig", Double.toString(s.getX())));
            this.addAttribute(new Attribute("y_orig", Double.toString(s.getY())));
        });
        return this.getLoc();
    }

    /**
     * Returns the element's ID, or assigns it a new one if not present.
     *
     * @return Unique ID
     */
    public final String getID() {
        var id = Optional.ofNullable(this.getAttribute("ID"))
                .map(f -> f.getValue());
        var myDoc = this.getDocument();
        var myDocEl = (DocElement) (myDoc.getRootElement());

        return id.isPresent() ? id.get() : myDocEl.getNewUniqueID(this.getLocalName());
    }

    /**
     * Set element ID.
     *
     * @param id ID to set
     * @return Set ID
     */
    public final String setID(final String id) {
        this.addAttribute(new Attribute("ID", id));
        return this.getID();
    }

    /**
     * Get the object's Z location. Default to 0 if not present.
     *
     * @return Z-index
     */
    public final Double getZInd() {
        var ind = this.getAttribute("z_ind");
        return (ind != null) ? Double.valueOf(ind.getValue()) : 0;
    }

    /**
     * Set the object's Z location. Returns set Z-index.
     *
     * @param z Index to set.
     * @return Set index.
     */
    public final Double setZInd(final Double z) {
        this.addAttribute(new Attribute("z_ind", Double.toString(z)));
        return this.getZInd();
    }

    /**
     * Get the object's size.Returns an Optional, which may contain either the
     * size or nothing. Rotation defaults to 0 if not present.
     *
     * @return Optional size
     */
    public final Optional<SizeObj> getSize() {
        var x = Optional.ofNullable(this.getAttribute("x_size_px"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));
        var y = Optional.ofNullable(this.getAttribute("y_size_px"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));
        var rot = Optional.ofNullable(this.getAttribute("rot_angle"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));

        return (x.isPresent() && y.isPresent())
                ? Optional.of(new SizeObj(x.get(),
                        y.get(),
                        rot.isPresent() ? rot.get() : 0))
                : Optional.empty();
    }

    /**
     * Get the element's fill colour.
     *
     * @return Optional colour.
     */
    public final Optional<Color> getFillColour() {
        final int lenRGB = 6;
        final int lenRGBA = 8;
        var col = Optional.ofNullable(this.getAttribute("fill"));
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
     * Get the local scope for this object.
     *
     * @return my Bindings.
     */
    @Override
    public final RecursiveBindings getScriptingBindings() {
        return elementScriptBindings;
    }

    /**
     * Get Local Script Bindings of parent node, if parent node is another
     * Scriptable element.
     *
     * @return Optional Bindings
     */
    @Override
    public final Optional<RecursiveBindings> getParentElementScriptingBindings() {
        var parent = this.getParent();
        if (parent instanceof Scriptable) {
            return Optional.of(((Scriptable) parent).getScriptingBindings());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get the ScriptElement attached to this object. There should only be one
     * element.
     *
     * @return my (first) script element.
     */
    @Override
    public final Optional<ScriptElement> getScriptEl() {
        var chEls = this.getChildElements();
        for (var ch : chEls) {
            if (ch instanceof ScriptElement) {
                return Optional.of((ScriptElement) ch);
            }
        }
        return Optional.empty();
    }

}
