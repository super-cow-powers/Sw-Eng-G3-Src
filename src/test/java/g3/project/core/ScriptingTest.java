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

import g3.project.elements.Scriptable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.tools.ant.taskdefs.MacroInstance.Element;
import org.mozilla.javascript.engine.RhinoScriptEngine;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class ScriptingTest {
    
    public ScriptingTest() {
    }
//CHECKSTYLE:OFF    
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
     * Test of evalString method, of class Scripting.
     */
    @Test
    public void testEvalString() throws Exception {
        System.out.println(RhinoScriptEngine.ENGINE_VERSION);
        System.out.println("evalString");
        String pyCode = "va = 3+2"
                + "\nprint(\"Python Test va = \" + str(va))";
        String pyLang = "python";
        String rhinoCode = "va = 3+2"
                + "\njava.lang.System.out.println(\"Rhino Test va = \"+va)";
        String rhinoLang = "rhino";
        RecursiveBindings pyBindings = new RecursiveBindings();
        RecursiveBindings rhinoBindings = new RecursiveBindings();
        
        Scripting instance = new Scripting("python", null);
        //Test Jython/python
        instance.evalString(pyCode, pyLang, pyBindings);
        //Test Rhino/JS
        instance.evalString(rhinoCode, rhinoLang, rhinoBindings);
        //CHECKSTYLE:OFF
        assertEquals(pyBindings.get("va"), 5);
        assertEquals(rhinoBindings.get("va"), 5);
        //CHECKSTYLE:ON
    }

    @Test
    public void testEvalElement() throws Exception {
        System.out.println(RhinoScriptEngine.ENGINE_VERSION);
        System.out.println("evalElement");
    }
    
}
