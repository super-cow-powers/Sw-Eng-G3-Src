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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class StyledTextSegTest {

    //CHECKSTYLE:OFF
    StyledTextSeg instance;
    static final String myText = "hello!";

    public StyledTextSegTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        instance = new StyledTextSeg(new FontProps(FontProps.PROP_DEFAULTS), myText);
    }

    @AfterEach
    public void tearDown() {
    }
    //CHECKSTYLE:ON

    /**
     * Test of setHRef method, of class StyledTextSeg.
     */
    @Test
    public void testSetHRef() {
        System.out.println("setHRef");
        String target = "www.wikipedia.org";
        StyledTextSeg.REF_TYPE type = StyledTextSeg.REF_TYPE.EXTERNAL;
        instance.setHRef(target, type);
        assertEquals(instance.getRefTarget(), target);
    }

    /**
     * Test of isHref method, of class StyledTextSeg.
     */
    @Test
    public void testIsHref() {
        System.out.println("isHref");
        Boolean expResult = false;
        Boolean result = instance.isHref();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRefTarget method, of class StyledTextSeg.
     */
    @Test
    public void testGetRefTarget() {
        System.out.println("getRefTarget");
        String target = "www.wikipedia.org";
        StyledTextSeg.REF_TYPE type = StyledTextSeg.REF_TYPE.EXTERNAL;
        instance.setHRef(target, type);
        assertEquals(instance.getRefTarget(), target);

        target = "page-0";
        type = StyledTextSeg.REF_TYPE.INTERNAL;
        instance.setHRef(target, type);
        assertEquals(instance.getRefTarget(), target);
    }

    /**
     * Test of getRefType method, of class StyledTextSeg.
     */
    @Test
    public void testGetRefType() {
        System.out.println("getRefType");
        String target = "www.wikipedia.org";
        StyledTextSeg.REF_TYPE type = StyledTextSeg.REF_TYPE.EXTERNAL;
        instance.setHRef(target, type);
        assertEquals(instance.getRefType(), type);

        target = "page-0";
        type = StyledTextSeg.REF_TYPE.INTERNAL;
        instance.setHRef(target, type);
        assertEquals(instance.getRefType(), type);
    }

    /**
     * Test of getStyle method, of class StyledTextSeg.
     */
    @Test
    public void testGetStyle() {
        System.out.println("getStyle");

        FontProps expResult = new FontProps(FontProps.PROP_DEFAULTS);
        FontProps result = instance.getStyle();
        assertEquals(expResult, result);
    }

    /**
     * Test of getString method, of class StyledTextSeg.
     */
    @Test
    public void testGetString() {
        System.out.println("getString");
        String result = instance.getString();
        assertEquals(myText, result);
    }

}
