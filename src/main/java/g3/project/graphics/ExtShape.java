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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class ExtShape extends Group {

    private StackPane stack = new StackPane();
    private Shape shape;
    private TextFlow textFlow = null;
    private Boolean amTextbox = false;

    private Double width;
    private Double height;

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

    public ExtShape(String shapeType, String ID, SizeObj size, Color fill, StrokeProps strokeProps, ArrayList<StyledTextSeg> text) {
        var newWidth = size.getX();
        var newHeight = size.getY();
        switch (shapeType) {
            case "circle":
                shape = new Ellipse(newWidth / 2, newHeight / 2);
                break;
            case "textbox":
                shape = new Rectangle(newWidth, newHeight);
                break;
            case "rectangle":
                shape = new Rectangle(newWidth, newHeight);
                break;
            default:
                shape = new Rectangle(newWidth, newHeight);
                break;

        }
        this.width = newWidth;
        this.height = newHeight;
        if (shape == null) {
            return;
        }
        this.setId(ID);
        System.out.println(strokeProps.toCSS());
        shape.setStyle(strokeProps.toCSS()); //Apply stroke CSS

        if (fill != null) {
            shape.setFill(fill);
        }
        stack.getChildren().add(shape);
        if (text.size() > 0) {
            setText(text);
        }

        this.getChildren().add(stack);
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

    /**
     * Set the shape size.
     *
     * @param size Size.
     */
    public final void setSize(final SizeObj size) {
        var newWidth = size.getX();
        var newHeight = size.getY();
        this.width = newWidth;
        this.height = newHeight;
        if (textFlow != null) {
            textFlow.setPrefWidth(width);
        }
        if (shape instanceof Rectangle) {
            ((Rectangle) shape).setWidth(width);
            ((Rectangle) shape).setHeight(height);
        } else if (shape instanceof Ellipse) {
            ((Ellipse) shape).setRadiusX(width / 2);
            ((Ellipse) shape).setRadiusY(height / 2);
        }
    }

    /**
     * Set the shape fill colour.
     *
     * @param fill Fill colour.
     */
    public final void setFill(final Color fill) {
        shape.setFill(fill);
    }

    /**
     * Configure shape stroke.
     *
     * @param strokeColor Colour of stroke.
     * @param strokeWidth Width of stroke.
     */
    public final void setStroke(final StrokeProps stroke) {
        shape.setStyle(stroke.toCSS());
    }

    /**
     * Set text and style in element.
     *
     * @todo Make work for arbitrary Rich Text.
     * @param text Text to set.
     */
    public final void setText(final ArrayList<StyledTextSeg> text) {
        if (textFlow == null) {
            textFlow = new TextFlow();
            stack.getChildren().add(textFlow);
        }
        textFlow.setPrefWidth(this.width);
        textFlow.getChildren().clear();
        textFlow.setStyle("-fx-background-color:transparent;");
        //Iterate through all segments
        for (final StyledTextSeg seg : text) {
            Node textEl;
            if (seg.isHref()) {
                textEl = new Hyperlink(seg.getString());
                ((Hyperlink) textEl).setOnMouseClicked(e -> hrefClickHandlerConsumer.accept(e));
                ((Hyperlink) textEl).setOnMouseEntered(e -> hrefHovEntHandlerConsumer.accept(e));
                ((Hyperlink) textEl).setOnMouseExited(e -> hrefHovExHandlerConsumer.accept(e));
            } else {
                textEl = new Text(seg.getString());
            }
            textEl.setStyle(seg.getStyle().toCSS());
            textFlow.getChildren().add(textEl);
        }

    }
}
