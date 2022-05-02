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
package g3.project.graphics;

import g3.project.graphics.StyledTextSeg.REF_TYPE;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class ExtShapeFactory {

    /**
     * href click handler.
     */
    @SuppressWarnings("empty-statement")
    private Consumer<MouseEvent> hrefClickHandlerConsumer = (evt) -> {
        ;
    };

    /**
     * href mouse roll-over (hover) enter handler.
     */
    @SuppressWarnings("empty-statement")
    private Consumer<MouseEvent> hrefHovEntHandlerConsumer = (evt) -> {
        ;
    };

    /**
     * href mouse roll-over (hover) exit handler.
     */
    @SuppressWarnings("empty-statement")
    private Consumer<MouseEvent> hrefHovExHandlerConsumer = (evt) -> {
        ;
    };

    /**
     * Shape types.
     */
    //CHECKSTYLE:OFF
    public enum ShapeType {
        circle,
        textbox,
        rectangle,
        polygon,
        line
    };
    //CHECKSTYLE:ON//CHECKSTYLE:ON

    public ExtShapeFactory() {

    }

    /**
     * Make a new extended shape.
     *
     * @param shapeType Type of shape to make.
     * @return Maybe shape.
     */
    public Optional<ExtShape> makeShape(final ShapeType shapeType) {
        ExtShape shape = null;
        switch (shapeType) {
            case circle:
                shape = new ExtEllip();
                break;
            case textbox:
                shape = new ExtRect();
                break;
            case rectangle:
                shape = new ExtRect();
                break;
            case polygon:
            case line:
                shape = new ExtPolygon();
            default:
                break;

        }
        var maybeShape = Optional.ofNullable(shape);
        maybeShape.ifPresent(sh -> {
            sh.setHrefClickHandler(hrefClickHandlerConsumer);
            sh.setHrefHoverEnterHandler(hrefHovEntHandlerConsumer);
            sh.setHrefHoverExitHandler(hrefHovExHandlerConsumer);
        });
        return maybeShape;
    }

    /**
     * Set the href click handler.
     *
     * @param handler Handler to set.
     */
    public final void setHrefClickHandler(final Consumer<MouseEvent> handler) {
        this.hrefClickHandlerConsumer = handler;
    }

    /**
     * Set the href hover entry handler.
     *
     * @param handler Handler to set.
     */
    public final void setHrefHoverEnterHandler(final Consumer<MouseEvent> handler) {
        this.hrefHovEntHandlerConsumer = handler;
    }

    /**
     * Set the href hover exit handler.
     *
     * @param handler Handler to set.
     */
    public final void setHrefHoverExitHandler(final Consumer<MouseEvent> handler) {
        this.hrefHovExHandlerConsumer = handler;
    }

}
