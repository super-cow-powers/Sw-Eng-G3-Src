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
package g3.project.core;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Random;
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
public final class RecursiveBindingsTest {

    private static Integer TEST_ITERATIONS = 1000;
    private static Integer MAX_ARR_LEN = 10000;

    //CHECKSTYLE:OFF
    public RecursiveBindingsTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() {

    }

    @AfterEach
    public void tearDown() {
    }
    //CHECKSTYLE:ON

    /**
     * Test of setParent method, of class RecursiveBindings.
     */
    @Test
    public void testSetParent() {
        System.out.println("setParent");
        RecursiveBindings p = new RecursiveBindings();
        RecursiveBindings instance = new RecursiveBindings();
        instance.setParent(p);
        //Check parent is present and correct
        assertEquals(instance.getParent().isPresent(), true);
        assertEquals(instance.getParent().get(), p);
    }

    /**
     * Test of localContainsKey method, of class RecursiveBindings.
     */
    @Test
    public void testLocalContainsKey() {
        RecursiveBindings instance = new RecursiveBindings();
        System.out.println("localContainsKey");
        Random rand = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            byte[] array = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            rand.nextBytes(array);
            String key = new String(array, Charset.forName("UTF-8"));
            instance.put(key, "");
            boolean expResult = true;
            boolean result = instance.localContainsKey(key);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of localGet method, of class RecursiveBindings.
     */
    @Test
    public void testLocalGet() {
        RecursiveBindings instance = new RecursiveBindings();
        System.out.println("localGet");
        Random rand = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            byte[] keyArray = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            byte[] valArray = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            rand.nextBytes(keyArray);
            rand.nextBytes(valArray);
            String key = new String(keyArray, Charset.forName("UTF-8"));
            String val = new String(valArray, Charset.forName("UTF-8"));
            instance.put(key, val);
            String result = (String) instance.localGet(key).get();
            assertEquals(val, result);
        }
    }

    /**
     * Test of containsKey method, of class RecursiveBindings.
     */
    @Test
    public void testContainsKey() {
        RecursiveBindings instance = new RecursiveBindings();
        RecursiveBindings parent = new RecursiveBindings();
        RecursiveBindings grandParent = new RecursiveBindings();
        parent.setParent(grandParent);
        instance.setParent(parent);
        System.out.println("containsKey");
        Random rand = new Random();
        //Test local
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            byte[] array = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            rand.nextBytes(array);
            String key = new String(array, Charset.forName("UTF-8"));
            instance.put(key, "key");
            boolean expResult = true;
            boolean result = instance.containsKey(key);
            assertEquals(expResult, result);
        }
        //Test parent
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            byte[] array = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            rand.nextBytes(array);
            String key = new String(array, Charset.forName("UTF-8"));
            parent.put(key, "key");
            boolean expResult = true;
            boolean result = instance.containsKey(key);
            assertEquals(expResult, result);
        }
        //Test grandparent
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            byte[] array = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            rand.nextBytes(array);
            String key = new String(array, Charset.forName("UTF-8"));
            grandParent.put(key, "key");
            boolean expResult = true;
            boolean result = instance.containsKey(key);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of get method, of class RecursiveBindings.
     */
    @Test
    public void testGet() {
        RecursiveBindings instance = new RecursiveBindings();
        RecursiveBindings parent = new RecursiveBindings();
        RecursiveBindings grandParent = new RecursiveBindings();
        parent.setParent(grandParent);
        instance.setParent(parent);
        System.out.println("get");
        Random rand = new Random();
        //Test local
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            byte[] keyArray = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            byte[] valArray = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            rand.nextBytes(keyArray);
            rand.nextBytes(valArray);
            String key = new String(keyArray, Charset.forName("UTF-8"));
            String val = new String(valArray, Charset.forName("UTF-8"));
            instance.put(key, val);
            var result = instance.get(key);
            assertEquals(val, result);
        }
        //Test parent
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            byte[] keyArray = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            byte[] valArray = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            rand.nextBytes(keyArray);
            rand.nextBytes(valArray);
            String key = new String(keyArray, Charset.forName("UTF-8"));
            String val = new String(valArray, Charset.forName("UTF-8"));
            parent.put(key, val);
            var result = instance.get(key);
            assertEquals(val, result);
        }
        //Test grandparent
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            byte[] keyArray = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            byte[] valArray = new byte[rand.nextInt(MAX_ARR_LEN) + 1];
            rand.nextBytes(keyArray);
            rand.nextBytes(valArray);
            String key = new String(keyArray, Charset.forName("UTF-8"));
            String val = new String(valArray, Charset.forName("UTF-8"));
            grandParent.put(key, val);
            var result = instance.get(key);
            assertEquals(val, result);
        }
    }

    /**
     * Test of getParent method, of class RecursiveBindings.
     */
    @Test
    public void testGetParent() {
        System.out.println("getParent");
        RecursiveBindings instance = new RecursiveBindings();
        RecursiveBindings p = new RecursiveBindings();
        //No parent.
        Optional<RecursiveBindings> result = instance.getParent();
        assertEquals(result.isEmpty(), true);
        //Set parent.
        instance.setParent(p);
        //Check parent is present and correct
        result = instance.getParent();
        assertEquals(result.isPresent(), true);
        assertEquals(result.get(), p);
        //null parent.
        instance.setParent(null);
        result = instance.getParent();
        assertEquals(result.isEmpty(), true);
    }

}
