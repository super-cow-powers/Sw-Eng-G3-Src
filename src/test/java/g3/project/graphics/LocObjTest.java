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

import java.util.Random;
import javafx.geometry.Point2D;
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
public class LocObjTest {

    //CHECKSTYLE:OFF
    static final Integer TEST_ITERATIONS = 1000;

    public LocObjTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }
//CHECKSTYLE:ON

    /**
     * Test of getLoc method, of class LocObj.
     */
    @Test
    public void testGetLoc() {
        System.out.println("getLoc");
        Random rand = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            Point2D expResult = new Point2D(rand.nextDouble(), rand.nextDouble());
            var instance = new LocObj(expResult, 0d);
            Point2D result = instance.getLoc();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getZ method, of class LocObj.
     */
    @Test
    public void testGetZ() {
        System.out.println("getZ");
        Random rand = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            Point2D point = new Point2D(0d, 0d);
            Double z = rand.nextDouble();
            var instance = new LocObj(point, z);
            Double result = instance.getZ();
            assertEquals(z, result);
        }
    }

}
