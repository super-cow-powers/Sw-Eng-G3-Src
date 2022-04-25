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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
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
     * General href handler. String is location.
     */
    @SuppressWarnings("empty-statement")
    private BiConsumer<String, REF_TYPE> hrefHandlerBiConsumer = (loc, type) -> {
        ;
    };

    public ExtShape(String shapeType, String ID, Double width, Double height, Color fill, Color strokeColour, Double strokeSize, ArrayList<StyledTextSeg> text) {
        switch (shapeType) {
            case "circle":
                shape = new Ellipse(width / 2, height / 2);
                break;
            case "textbox":
                shape = new Rectangle(width, height);
                break;
            case "rectangle":
                shape = new Rectangle(width, height);
                break;
            default:
                shape = new Rectangle(width, height);
                break;

        }
        this.width = width;
        this.height = height;
        if (shape == null) {
            return;
        }
        this.setId(ID);
        if (strokeColour != null) {
            shape.setStroke(strokeColour);
        }
        if (strokeSize != null) {
            shape.setStrokeWidth(strokeSize);
        }
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
     * Set the href handler.
     *
     * @param handler Handler to set.
     */
    public void setHrefHandler(BiConsumer<String, REF_TYPE> handler) {
        this.hrefHandlerBiConsumer = handler;
    }

    /**
     * Set the shape size.
     *
     * @param width Width.
     * @param height Height.
     */
    public void setSize(final Double width, final Double height) {
        this.width = width;
        this.height = height;
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
    public final void setStroke(final Color strokeColor, final Double strokeWidth) {
        shape.setStroke(strokeColor);
        shape.setStrokeWidth(strokeWidth);
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
        for (StyledTextSeg seg : text) {
            Node textEl;
            if (seg.isHref()) {
                textEl = new Hyperlink(seg.getString());
                var refType = seg.getRefType();
                final String refTgt = seg.getRefTarget();
                ((Hyperlink) textEl).setOnAction(e -> hrefHandlerBiConsumer.accept(refTgt, refType));
            } else {
                textEl = new Text(seg.getString());
            }
            textEl.setStyle(seg.getStyle().toCSS());
            textFlow.getChildren().add(textEl);
        }

    }
}
