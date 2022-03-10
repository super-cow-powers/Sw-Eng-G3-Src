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

import g3.project.ui.ObjSize;
import java.util.Optional;
import javafx.geometry.Point2D;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class VisualElement extends Element {

    public VisualElement(String name) {
        super(name);
    }

    public VisualElement(String name, String uri) {
        super(name, uri);
    }

    public VisualElement(Element element) {
        super(element);
    }

    /** 
     * Get the object's X/Y location.
     * Returns an Optional, which may contain either the location or nothing.
     * The caller can then determine the action to take.
     */ 
    public Optional<Point2D> getLoc() {
        var x = Optional.ofNullable(this.getAttribute("x_orig"))
                .map(f->f.getValue())
                .map(f->Double.valueOf(f));
        var y = Optional.ofNullable(this.getAttribute("y_orig"))
                .map(f->f.getValue())
                .map(f->Double.valueOf(f));
        
        return (x.isPresent() && y.isPresent())? 
                Optional.of(new Point2D(x.get(),y.get())) : Optional.empty();
    }
    /** 
     * Set the object's X/Y location. Returns the new location.
     */ 
    public Optional<Point2D> setLoc(Point2D loc) {
        var x = loc.getX();
        var y = loc.getY();
        
        this.addAttribute(new Attribute("x_orig",Double.toString(x)));
        this.addAttribute(new Attribute("y_orig",Double.toString(y)));
        return this.getLoc();
    }
    public Optional<String> getID() {
        var ID = Optional.ofNullable(this.getAttribute("ID"))
                .map(f->f.getValue());
        return ID;
    }
    
    /** 
     * Get the object's Z location.
     * Default to 0 if not present.
     */ 
    public Double getZInd(){
        var ind = this.getAttribute("z_ind");
        return (ind != null) ? Double.valueOf(ind.getValue()) : 0;
    }
    /** 
     * Set the object's Z location. Returns the new location.
     */ 
    public Double setZInd(Double z) {
        this.addAttribute(new Attribute("z_ind", Double.toString(z)));
        return this.getZInd();
    }
    
    /** 
     * Get the object's size.
     * Returns an Optional, which may contain either the size or nothing.
     * Rotation defaults to 0 if not present.
     */ 
    public Optional<ObjSize> getSize() {
        var x = Optional.ofNullable(this.getAttribute("x_size_px"))
                .map(f->f.getValue())
                .map(f->Double.valueOf(f));
        var y = Optional.ofNullable(this.getAttribute("y_size_px"))
                .map(f->f.getValue())
                .map(f->Double.valueOf(f));
        var rot = Optional.ofNullable(this.getAttribute("rot_angle"))
                .map(f->f.getValue())
                .map(f->Double.valueOf(f));
        
        return (x.isPresent() && y.isPresent())? 
                    Optional.of(new ObjSize(x.get(),
                                    y.get(),
                                    rot.isPresent()? rot.get() : 0))
                    : Optional.empty();
    }
    
}
