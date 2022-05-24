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

import g3.project.core.Engine;
import g3.project.core.RecursiveBindings;
import g3.project.graphics.LocObj;
import g3.project.graphics.SizeObj;
import g3.project.graphics.StrokeProps;
import g3.project.graphics.VisualProps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class VisualElement extends Element implements Scriptable {

    static public final String BASE_URI = "http://PWS_Base";
    static public final String EXT_URI = "http://PWS_Exts";

    /**
     * Script bindings for the element.
     */
    private RecursiveBindings elementScriptBindings = new RecursiveBindings();

    /**
     * Ref to the engine.
     */
    private Optional<Engine> engine = Optional.empty();

    /**
     * Does the script need evaluating again?
     */
    private Boolean evalRequired = true;

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
     * Find qualified attribute from element.
     *
     * @param el Element to use.
     * @param qualifiedName Full attribute name.
     * @return Maybe attribute.
     */
    public static Optional<Attribute> derefAttribute(final Element el, final String qualifiedName) {
        var nameSplit = qualifiedName.split(":");
        var attrNS = (nameSplit.length > 1) ? EXT_URI : "";
        var attrName = (nameSplit.length > 1) ? nameSplit[1] : nameSplit[0];
        var attr = el.getAttribute(attrName, attrNS);
        return Optional.ofNullable(attr);
    }

    /**
     * Get the object's X/Y location. Returns an Optional, which may contain
     * either the location or nothing. The caller can then determine the action
     * to take.
     *
     * @return Optional Location
     */
    public final Optional<LocObj> getOrigin() {
        var x = Optional.ofNullable(this.getAttribute("x_orig"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));
        var y = Optional.ofNullable(this.getAttribute("y_orig"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));

        return (x.isPresent() && y.isPresent())
                ? Optional.of(new LocObj(new Point2D(x.get(), y.get()), getZInd())) : Optional.empty();
    }

    /**
     * Set the object's X/Y location.
     *
     * @param loc Location to set
     */
    public final void setOrigin(final LocObj loc) {
        var point = loc.getLoc();

        this.addAttribute(new Attribute("x_orig", Double.toString(point.getX())));
        this.addAttribute(new Attribute("y_orig", Double.toString(point.getY())));
        hasUpdated();
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
        hasUpdated();
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
        hasUpdated();
        return this.getZInd();
    }

    /**
     * Get the object's size.Returns an Optional, which may contain either the
     * size or nothing. Rotation defaults to 0 if not present.
     *
     * @return Optional size
     */
    public final Optional<SizeObj> getSize() {
        var xOpt = Optional.ofNullable(this.getAttribute("x_size_px"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));
        var yOpt = Optional.ofNullable(this.getAttribute("y_size_px"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));
        var rotOpt = Optional.ofNullable(this.getAttribute("rot_angle"))
                .map(f -> f.getValue())
                .map(f -> Double.valueOf(f));

        if (xOpt.isEmpty() && yOpt.isEmpty() && rotOpt.isEmpty()) {
            //Really is no size given.
            return Optional.empty();
        }
        //Sometimes element may give only some params - set others to 0.
        final Double x = (xOpt.isEmpty()) ? 0 : xOpt.get();
        final Double y = (yOpt.isEmpty()) ? 0 : yOpt.get();
        final Double rot = (rotOpt.isEmpty()) ? 0 : rotOpt.get();

        return Optional.of(new SizeObj(x, y, rot));
    }

    /**
     * Set the element size.
     *
     * @param size Size to set.
     */
    public final void setSize(final SizeObj size) {
        Attribute xAttr = new Attribute("x_size_px", size.getX().toString());
        Attribute yAttr = new Attribute("y_size_px", size.getY().toString());
        Attribute rotAttr = new Attribute("rot_angle", size.getRot().toString());
        this.addAttribute(xAttr);
        this.addAttribute(yAttr);
        this.addAttribute(rotAttr);
        hasUpdated();
    }

    /**
     * Get the element's fill colour.
     *
     * @return Optional colour.
     */
    public final Optional<Color> getFillColour() {
        var colAttr = this.getAttribute("fill");
        /**
         * @todo: Find a nicer looking way of making this work Probably
         * containing more streams.
         */
        if (colAttr != null) {

            //var colStr = colAttr.get().getValue().replace("#", "");
            var colStr = colAttr.getValue();
            Color col = null;
            try {
                col = Color.web(colStr);
            } catch (IllegalArgumentException ex) {
                System.err.println("Bad Colour: " + ex);
            }
            return Optional.ofNullable(col);
        }
        return Optional.empty();
    }

    /**
     * Set the fill colour.
     *
     * @param colourString RGB or RGBA string.
     * @throws Exception Bad colour string.
     */
    public final void setFillColour(final String colourString) throws Exception {
        if (!colourString.startsWith("#")) {
            throw new Exception("Bad Colour String");
        }
        var colAttr = new Attribute("fill", colourString);
        this.addAttribute(colAttr);
        hasUpdated();
    }

    /**
     * Returns the referred element, if this is it or it is a child of this.
     *
     * @param id Element to find.
     * @return Optional referred element.
     */
    public final Optional<VisualElement> getByID(final String id) {
        if (this.getID() == id) {
            return Optional.of(this);
        } else {
            for (Element el : this.getChildElements()) {
                if (el instanceof VisualElement) {
                    var elOp = ((VisualElement) el).getByID(id);
                    if (elOp.isPresent()) {
                        return elOp; //Found it
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get the stroke element for this element.
     *
     * @return Optional Stroke Element.
     */
    public final Optional<StrokeProps> getStroke() {
        for (Element el : this.getChildElements()) {
            if (el instanceof StrokeElement) {
                return Optional.of(((StrokeElement) el).getProps());
            }
        }
        return Optional.empty();
    }

    /**
     * Get this element's visual properties map.
     *
     * @return visual props. map.
     */
    public final VisualProps getVisualProps() {
        var propsMap = new VisualProps();
        for (String prop : propsMap.getPropsTypes().keySet()) {
            switch (prop) {
                //Special cases
                default: //Not a special case
                    var attrMaybe = derefAttribute(this, prop);
                    //this.getAttribute(prop, prop)
                    if (attrMaybe.isPresent()) {
                        var attr = attrMaybe.get();
                        var attrVal = attr.getValue();
                        Class attrType = propsMap.getPropsTypes().get(prop);
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
     * Get all properties of an Element.
     *
     * @return Element's properties.
     */
    public HashMap<String, HashMap<String, Object>> getAllProps() {
        HashMap<String, HashMap<String, Object>> propsMap = new HashMap<>();
        propsMap.put("visual", getVisualProps());
        return propsMap;
    }

    /**
     * Set this object's properties. This default implementation will set Visual
     * Props. Override, call super(...), then set further props for extensions.
     *
     * @param props Properties.
     */
    public void setProps(final HashMap<String, Object> props) {

    }

    /**
     * Element has changed/updated. Notify the engine.
     */
    protected final void hasUpdated() {
        var root = this.getDocument().getRootElement();
        if (root instanceof DocElement) {
            ((DocElement) root).getChangeCallback().accept(this);
        }
    }

    /**
     * Get the local scope for this object.
     *
     * @return my Bindings.
     */
    @Override
    public final RecursiveBindings getScriptingBindings() {
        var parBinOpt = this.getParentElementScriptingBindings();
        parBinOpt.ifPresent(b -> elementScriptBindings.setParent(b)); //Always set parent bindings
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

    @Override
    public final String getRealType() {
        return this.getClass().getName();
    }

    @Override
    public void addScriptFile(Path path, String language) throws IOException {
        if (!path.getFileSystem().provider().getScheme().contains("jar") && !path.getFileSystem().provider().getScheme().contains("zip")) {
            throw new IOException("External files not supported. Add the file to the project.");
        }
        ScriptElement scEl = new ScriptElement("ext:script", VisualElement.EXT_URI, path.toString(), language);
        var chEls = this.getChildElements();
        //Remove other scripts.
        for (var ch : chEls) {
            if (ch instanceof ScriptElement) {
                this.removeChild(ch);
            }
        }
        this.appendChild(scEl);
    }

    /**
     * Should I be re-evaluated?
     *
     * @return To eval.
     */
    @Override
    public Boolean getEvalRequired() {
        return evalRequired;
    }

    /**
     * Set if I should be re-evaluated.
     *
     * @param req Re-eval?
     */
    @Override
    public void setEvalRequired(final Boolean req) {
        evalRequired = req;
    }

}
