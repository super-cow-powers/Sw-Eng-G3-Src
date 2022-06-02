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

import g3.project.ui.Visual;
import java.util.ArrayList;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public abstract class ExtShape extends Group implements Visual {

    private StackPane stack = new StackPane();
    private Shape shape = null;
    private TextFlow textFlow = null;
    private VBox textVbox = null;

    private Double width = 0d;
    private Double height = 0d;
    private Double rot = 0d;

    /**
     * text click handler.
     */
    private Consumer<MouseEvent> textClickHandlerConsumer = null;

    /**
     * href click handler.
     */
    private Consumer<MouseEvent> hrefClickHandlerConsumer = null;

    /**
     * href mouse roll-over (hover) enter handler.
     */
    private Consumer<MouseEvent> hrefHovEntHandlerConsumer = null;

    /**
     * href mouse roll-over (hover) exit handler.
     */
    private Consumer<MouseEvent> hrefHovExHandlerConsumer = null;

    /**
     * Constructor
     * @param myShape
     */
    public ExtShape(Shape myShape) {
        shape = myShape;
        stack.getChildren().add(shape);
        this.getChildren().add(stack);
    }

    /**
     * Set the text click handler.
     *
     * @param handler Handler to set.
     */
    public final void setTextClickHandler(final Consumer<MouseEvent> handler) {
        this.textClickHandlerConsumer = handler;
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
    @Override
    public abstract void setSize(final SizeObj size);

    /**
     * Set the shape fill colour.
     *
     * @param fill Fill colour.
     */
    public final void setFill(final Color fill) {
        shape.setFill(fill);
    }

    /**
     * Getter for Stack
     * @return stack
     */
    public StackPane getStack() {
        return(stack);
    }

    /**
     * Getter for Shape
     * @return shape
     */
    public Shape getShape() {
        return(shape);
    }

    /**
     * Getter for TextFlow
     * @return textFlow
     */
    public TextFlow getTextFlow() {
        return(textFlow);
    }

    /**
     * Getter for textVbox
     * @return textVbox
     */
    public VBox getTextVBox() {
        return(textVbox);
    }

    /**
     * Getter for width
     * @return width
     */
    public Double getWidth() {
        return(width);
    }

    /**
     * Getter for height
     * @return height
     */
    public Double getHeight() {
        return(height);
    }

    /**
     * Getter for rotation
     * @return rotation
     */
    public Double getRot() {
        return(rot);
    }

    /**
     * Configure shape stroke.
     *
     * @param stroke stroke properties.
     */
    public final void setStroke(final StrokeProps stroke) {
        shape.setStroke((Color) stroke.getProp(StrokeProps.COLOUR).get());
        shape.setStrokeLineCap(StrokeLineCap.valueOf(((String) stroke.getProp(StrokeProps.LINE_CAP).get()).toUpperCase()));
        shape.setStrokeType(StrokeType.OUTSIDE);
        shape.getStrokeDashArray().clear();
        shape.setStrokeWidth((Double) stroke.getProp(StrokeProps.WIDTH).get());
        var dashArray = (String) stroke.getProp(StrokeProps.LINE_STYLE).get();
        for (String val : dashArray.split(" ")) { //Build dash array
            Double num;
            try {
                num = Double.parseDouble(val);
                shape.getStrokeDashArray().add(num);
            } catch (NumberFormatException e) {
            }
        }
    }

    /**
     * Set visual properties.
     *
     * @param visualProps properties.
     */
    public final void setVisualProps(final VisualProps visualProps) {
        var maybeShadow = visualProps.makeShadow();
        maybeShadow.ifPresent(sh -> {
            shape.setEffect(sh);
        }); //Apply shadow
        this.setVisible((Boolean) visualProps.getProp(VisualProps.VISIBLE).get());
        this.setFill((Color) visualProps.getProp(VisualProps.FILL).get());
    }

    /**
     * Set text and style in element.
     *
     * @todo Make work for arbitrary Rich Text.
     * @param text Text to set.
     * @param align Text horizontal alignment.
     * @param textVertAlign Text vertical alignment.
     */
    public final void setText(final ArrayList<StyledTextSeg> text, final TextAlignment align, final Pos textVertAlign) {
        if (text.size() <= 0) {
            return;
        }
        if (textFlow == null) {
            textFlow = new TextFlow();
            textVbox = new VBox();
            textVbox.getChildren().add(textFlow);
            textVbox.setPrefWidth(this.width);
            textFlow.setPrefWidth(this.width);
            stack.getChildren().add(textVbox);
        }
        textVbox.setAlignment(textVertAlign);
        textFlow.getChildren().clear();
        textFlow.setTextAlignment(align);
        textFlow.setOnMouseClicked(e -> textClickHandlerConsumer.accept(e));
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
