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

import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.input.MouseEvent;
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
public class ExtShapeFactoryTest {

    //CHECKSTYLE:OFF
    private ExtShapeFactory fact;

    public ExtShapeFactoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        fact = new ExtShapeFactory();
    }

    @AfterEach
    public void tearDown() {
    }
//CHECKSTYLE:ON

    /**
     * Test of makeShape method, of class ExtShapeFactory.
     */
    @Test
    public void testMakeShape() {
        System.out.println("Make Shape");
        for (var s : ExtShapeFactory.ShapeType.values()) {
            System.out.println(s.toString());
            Optional<ExtShape> shape = fact.makeShape(s);
            assertTrue(shape.isPresent());
            shape.ifPresent(sh -> assertTrue(sh.getShapeType().equals(s)));
        }
    }

    /**
     * Test of setTextClickHandler method, of class ExtShapeFactory.
     */
    @Test
    public void testSetTextClickHandler() {
        System.out.println("Text Click Handler");
        Consumer<MouseEvent> handler = (MouseEvent e) -> assertTrue(true);
        fact.setTextClickHandler(handler);
        fact.textClickHandlerConsumer.accept(null);
    }

    /**
     * Test of setHrefClickHandler method, of class ExtShapeFactory.
     */
    @Test
    public void testSetHrefClickHandler() {
        System.out.println("HRef Click Handler");
        Consumer<MouseEvent> handler = (MouseEvent e) -> assertTrue(true);
        fact.setHrefClickHandler(handler);
        fact.hrefClickHandlerConsumer.accept(null);
    }

    /**
     * Test of setHrefHoverEnterHandler method, of class ExtShapeFactory.
     */
    @Test
    public void testSetHrefHoverEnterHandler() {
        System.out.println("HRef Hover Enter Handler");
        Consumer<MouseEvent> handler = (MouseEvent e) -> assertTrue(true);
        fact.setHrefHoverEnterHandler(handler);
        fact.hrefHovEntHandlerConsumer.accept(null);
    }

    /**
     * Test of setHrefHoverExitHandler method, of class ExtShapeFactory.
     */
    @Test
    public void testSetHrefHoverExitHandler() {
        System.out.println("HRef Hover Exit Handler");
        Consumer<MouseEvent> handler = (MouseEvent e) -> assertTrue(true);
        fact.setHrefHoverExitHandler(handler);
        fact.hrefHovExHandlerConsumer.accept(null);
    }

}
