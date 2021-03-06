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

import static g3.project.graphics.LocObjTest.TEST_ITERATIONS;
import java.util.Random;
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
public class SizeObjTest {

    //CHECKSTYLE:OFF
    static final Integer TEST_ITERATIONS = 1000;

    public SizeObjTest() {
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
     * Test of getX method, of class SizeObj.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");
        Random rand = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            var x = rand.nextDouble();
            var y = rand.nextDouble();
            var rot = rand.nextDouble();
            var inst = new SizeObj(x, y, rot);
            assertEquals(x, inst.getX());
        }
    }

    /**
     * Test of getY method, of class SizeObj.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");
        Random rand = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            var x = rand.nextDouble();
            var y = rand.nextDouble();
            var rot = rand.nextDouble();
            var inst = new SizeObj(x, y, rot);
            assertEquals(y, inst.getY());
        }
    }

    /**
     * Test of getRot method, of class SizeObj.
     */
    @Test
    public void testGetRot() {
        Random rand = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            var x = rand.nextDouble();
            var y = rand.nextDouble();
            var rot = rand.nextDouble();
            var inst = new SizeObj(x, y, rot);
            assertEquals(rot % 360, inst.getRot());
        }
    }

}
