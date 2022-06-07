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
package g3.project.graphics;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Group 3
 */
public class ExtShapeTest {

    ExtShape instance;

    //CHECKSTYLE:OFF
    public ExtShapeTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new ExtRect(ExtShapeFactory.ShapeType.rectangle);
    }

    @AfterEach
    public void tearDown() {
    }

    //CHECKSTYLE:ON
    /**
     * Test of getShapeType method, of class ExtShape.
     */
    @Test
    public void testGetShapeType() {
        System.out.println("getShapeType");
        ExtShapeFactory.ShapeType expResult = ExtShapeFactory.ShapeType.rectangle;
        ExtShapeFactory.ShapeType result = instance.getShapeType();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTextClickHandler method, of class ExtShape.
     */
    @Test
    public void testSetTextClickHandler() {
        System.out.println("setTextClickHandler");
        Consumer<MouseEvent> handler = (MouseEvent e) -> assertTrue(true);
        instance.setTextClickHandler(handler);
        instance.textClickHandlerConsumer.accept(null);
    }

    /**
     * Test of setHrefClickHandler method, of class ExtShape.
     */
    @Test
    public void testSetHrefClickHandler() {
        System.out.println("setHrefClickHandler");
        Consumer<MouseEvent> handler = (MouseEvent e) -> assertTrue(true);
        instance.setHrefClickHandler(handler);
        instance.hrefClickHandlerConsumer.accept(null);
    }

    /**
     * Test of setHrefHoverEnterHandler method, of class ExtShape.
     */
    @Test
    public void testSetHrefHoverEnterHandler() {
        System.out.println("setHrefHoverEnterHandler");
        Consumer<MouseEvent> handler = (MouseEvent e) -> assertTrue(true);
        instance.setHrefHoverEnterHandler(handler);
        instance.hrefHovEntHandlerConsumer.accept(null);
    }

    /**
     * Test of setHrefHoverExitHandler method, of class ExtShape.
     */
    @Test
    public void testSetHrefHoverExitHandler() {
        System.out.println("setHrefHoverExitHandler");
        Consumer<MouseEvent> handler = (MouseEvent e) -> assertTrue(true);
        instance.setHrefHoverExitHandler(handler);
        instance.hrefHovExHandlerConsumer.accept(null);
    }

    /**
     * Test of setSize method, of class ExtShape.
     */
    @Test
    public void testSetSize() {
        System.out.println("setSize");
        Random rand = new Random();
        var x = rand.nextDouble();
        var y = rand.nextDouble();
        var rot = rand.nextDouble();
        SizeObj size = new SizeObj(x, y, rot);
        instance.setSize(size);
        assertEquals(instance.getWidth(), x);
        assertEquals(instance.getHeight(), y);
        assertEquals(instance.getRot(), rot);
    }

    /**
     * Test of setFill method, of class ExtShape.
     */
    @Test
    public void testSetFill() {
        System.out.println("setFill");
        Color fill = Color.BLANCHEDALMOND;
        instance.setFill(fill);
        assertTrue(instance.getShape().getFill().equals(fill));
    }

    /**
     * Test of getStack method, of class ExtShape.
     */
    @Test
    public void testGetStack() {
        System.out.println("getStack");
        Object result = instance.getStack();
        assertTrue(result instanceof StackPane);
    }

    /**
     * Test of getShape method, of class ExtShape.
     */
    @Test
    public void testGetShape() {
        System.out.println("getShape");
        Object result = instance.getShape();
        assertTrue(result instanceof Rectangle);
    }

    /**
     * Test of getTextFlow method, of class ExtShape.
     */
    @Test
    public void testGetTextFlow() {
        System.out.println("getTextFlow");
        var segs = new ArrayList<StyledTextSeg>();
        segs.add(new StyledTextSeg(new FontProps(FontProps.PROP_DEFAULTS), "hello!"));
        instance.setText(segs, TextAlignment.LEFT, Pos.CENTER_LEFT);
        assertTrue(instance.getTextFlow() != null);
    }

    /**
     * Test of getTextVBox method, of class ExtShape.
     */
    @Test
    public void testGetTextVBox() {
        System.out.println("getTextVBox");
        var segs = new ArrayList<StyledTextSeg>();
        segs.add(new StyledTextSeg(new FontProps(FontProps.PROP_DEFAULTS), "hello!"));
        instance.setText(segs, TextAlignment.LEFT, Pos.CENTER_LEFT);
        assertTrue(instance.getTextVBox() != null);
    }

    /**
     * Test of getWidth method, of class ExtShape.
     */
    @Test
    public void testGetWidth() {
        System.out.println("getWidth");
        Random rand = new Random();
        var x = rand.nextDouble();
        var y = rand.nextDouble();
        var rot = rand.nextDouble();
        SizeObj size = new SizeObj(x, y, rot);
        instance.setSize(size);
        assertEquals(instance.getWidth(), x);
    }

    /**
     * Test of getHeight method, of class ExtShape.
     */
    @Test
    public void testGetHeight() {
        System.out.println("getHeight");
        Random rand = new Random();
        var x = rand.nextDouble();
        var y = rand.nextDouble();
        var rot = rand.nextDouble();
        SizeObj size = new SizeObj(x, y, rot);
        instance.setSize(size);
        assertEquals(instance.getHeight(), y);
    }

    /**
     * Test of getRot method, of class ExtShape.
     */
    @Test
    public void testGetRot() {
        System.out.println("getRot");
        Random rand = new Random();
        var x = rand.nextDouble();
        var y = rand.nextDouble();
        var rot = rand.nextDouble();
        SizeObj size = new SizeObj(x, y, rot);
        instance.setSize(size);
        assertEquals(instance.getRot(), rot);
    }

    /**
     * Test of setStroke method, of class ExtShape.
     */
    @Test
    public void testSetStroke() {
        System.out.println("setStroke");
        StrokeProps stroke = new StrokeProps(StrokeProps.PROP_DEFAULTS);
        instance.setStroke(stroke);
        assertEquals(instance.getShape().strokeProperty().get().toString(), stroke.getProp(StrokeProps.COLOUR).get().toString());
    }

    /**
     * Test of setVisualProps method, of class ExtShape.
     */
    @Test
    public void testSetVisualProps() {
        System.out.println("setVisualProps");
        VisualProps visualProps = new VisualProps(VisualProps.PROP_DEFAULTS);
        instance.setVisualProps(visualProps);
        assertEquals(instance.visibleProperty().get(), visualProps.getProp(VisualProps.VISIBLE).get());
        assertEquals(instance.getShape().getFill().toString(), visualProps.getProp(VisualProps.FILL).get().toString());
    }

    /**
     * Test of setText method, of class ExtShape.
     */
    @Test
    public void testSetText() {
        String testText = "hello!";
        System.out.println("setText");
        var segs = new ArrayList<StyledTextSeg>();
        segs.add(new StyledTextSeg(new FontProps(FontProps.PROP_DEFAULTS), testText));
        instance.setText(segs, TextAlignment.LEFT, Pos.CENTER_LEFT);
        assertEquals(instance.getTextFlow().getChildren().size(), 1);
        if (instance.getTextFlow().getChildren().size() >= 1) {
            var el = instance.getTextFlow().getChildren().get(0);
            assertTrue(el instanceof Text);
            if (el instanceof Text) {
                assertEquals(((Text) el).getText(), testText);
            }
        }
    }

}
