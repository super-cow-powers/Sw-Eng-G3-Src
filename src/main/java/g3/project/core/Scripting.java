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
import g3.project.xmlIO.Io;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class Scripting {

    private static final String CLICK_FN = "onClick";

    /**
     * Factory/manager for all script engines.
     */
    private ScriptEngineManager scriptingEngineManager;

    /**
     * Instantiated Script Engines.
     */
    private HashMap<String, ScriptEngine> knownScriptEngines = new HashMap<>();

    /**
     * Ref to engine object.
     */
    private final Engine engine;

    /**
     * Constructor.
     *
     * @todo: Handle case of unknown language.
     *
     * @param defaultLang Default scripting language.
     * @param globalEngine Ref to the engine.
     */
    public Scripting(final String defaultLang, final Engine globalEngine) {
        // Init script engine manager
        scriptingEngineManager = new ScriptEngineManager();
        var globals = scriptingEngineManager.getBindings();
        globals.put("engine", globalEngine);
        engine = globalEngine;
        getScriptEngine(defaultLang); //pre-init a script engijne.
    }

    /**
     * Evaluate the script on a given element. Required before invoking any
     * functions.
     *
     * @param element Element to eval script of.
     * @throws ScriptException Couldn't eval script.
     * @throws IOException Couldn't get script.
     */
    public void evalElement(final Scriptable element) throws ScriptException, IOException {
        Io docIo = engine.getDocIO();
        var scrElOpt = element.getScriptEl();
        if (scrElOpt.isPresent()) {
            var scrEl = scrElOpt.get();
            //Setup bindings
            var bindings = element.getScriptingBindings();
            bindings.put("this", element);

            element.getParentElementScriptingBindings().ifPresent(p -> bindings.setParent(p));

            var scrEngine = getScriptEngine(scrEl.getScriptLang());
            scrEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

            var locOpt = scrEl.getSourceLoc();
            if (locOpt.isEmpty()) {
                throw new IOException("No script file specified");
            }
            var loc = locOpt.get();
            var bytesOpt = docIo.getResource(loc);
            if (bytesOpt.isPresent()) {
                var b = bytesOpt.get();
                scrEngine.eval(new String(b, StandardCharsets.UTF_8));
            } else {
                throw new IOException("Couldn't open script file");
            }
        }
    }

    /**
     * Execute element's onClick function.
     *
     * @param element Element to use.
     * @param button_name Mouse button name.
     * @param x_loc x location.
     * @param y_loc y location.
     */
    public void execElementClick(final Scriptable element, final String button_name, final Double x_loc, final Double y_loc) {
        var scrElOpt = element.getScriptEl();
        if (scrElOpt.isPresent()) {
            var scrEl = scrElOpt.get();
            var lang = scrEl.getScriptLang();
            var engine = getScriptEngine(lang);
            engine.setBindings(element.getScriptingBindings(), ScriptContext.ENGINE_SCOPE);
            try {
                ((Invocable) engine).invokeFunction(CLICK_FN, button_name, x_loc, y_loc);
            } catch (ScriptException | NoSuchMethodException ex) {
                Logger.getLogger(Scripting.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get a script engine for the specified language.
     *
     * @param lang language.
     * @return engine.
     */
    private ScriptEngine getScriptEngine(final String lang) {
        ScriptEngine scrEngine;

        if (knownScriptEngines.containsKey(lang)) {
            scrEngine = knownScriptEngines.get(lang);
        } else {
            scrEngine = scriptingEngineManager.getEngineByName(lang);
            knownScriptEngines.put(lang, scrEngine);
        }
        return scrEngine;
    }
}
