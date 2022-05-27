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
import g3.project.xmlIO.DocIO;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
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

    public static final String CLICK_FN = "onClick";
    public static final String KEY_PRESS_FN = "onKeyPress";
    public static final String MOUSE_MOVED_FN = "onMouseMoved";
    public static final String MOUSE_ENTER_FN = "onMouseEnter";
    public static final String MOUSE_EXIT_FN = "onMouseExit";
    public static final String DRAG_FUNCTION = "onDrag";
    public static final String LOAD_FUNCTION = "onLoad";

    /**
     * Factory/manager for all script engines.
     */
    private ScriptEngineManager scriptingEngineManager;

    /**
     * Instantiated Script Engines.
     */
    private HashMap<String, ScriptEngine> knownScriptEngines = new HashMap<>();

    /**
     * Top-level bindings to put base functions into.
     */
    private final RecursiveBindings topLevelBindings = new RecursiveBindings();

    private final String defaultLang;

    private final Writer defaultWriter;
    /**
     * Ref to engine object.
     */
    private final Engine engine;

    /**
     * Constructor.
     *
     * @todo: Handle case of unknown language.
     *
     * @param defaultLanguage Default scripting language.
     * @param globalEngine Ref to the engine.
     * @param writer Default Output writer.
     */
    public Scripting(final String defaultLanguage, final Engine globalEngine, final Writer writer) {
        // Init script engine manager
        scriptingEngineManager = new ScriptEngineManager();
        var globals = scriptingEngineManager.getBindings();
        globals.put("engine", globalEngine);
        engine = globalEngine;
        defaultLang = defaultLanguage;
        defaultWriter = writer;
        //Load in the custom global functions
        var fns = DocIO.getInternalResource("globalFunctions.py", Scripting.class);
        try {
            if (fns.isEmpty()) {
                throw new IOException("Couldn't get functions file");
            }
            var fnStr = new String(fns.get(), StandardCharsets.UTF_8);
            this.evalString(fnStr, defaultLanguage, topLevelBindings);
        } catch (IOException | NullPointerException | ScriptException ex) {
            //Default function loading failed.
            Logger.getLogger(DocIO.class.getName()).log(Level.SEVERE, null, ex);
            //Pre-init a script engine.
            getScriptEngine(defaultLanguage);
        }

    }

    /**
     * Add an object to global bindings.
     *
     * @param name Object name.
     * @param glob Object.
     */
    public void setGlobal(final String name, final Object glob) {
        scriptingEngineManager.getBindings().put(name, glob);
    }

    /**
     * Get a global variable.
     *
     * @param name Variable name.
     * @return Maybe variable.
     */
    public Optional<Object> getGlobal(final String name) {
        var globalVar = scriptingEngineManager.getBindings().get(name);
        return Optional.ofNullable(globalVar);
    }

    /**
     * Get Scripting global/top-level bindings.
     *
     * @return RecursiveBindings
     */
    public RecursiveBindings getTopLevelBindings() {
        return topLevelBindings;
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
        DocIO docIo = engine.getDocIO();
        var scrElOpt = element.getScriptEl();
        //Setup bindings
        var bindings = element.getScriptingBindings();
        bindings.put("this", element);
        element.getParentElementScriptingBindings().ifPresent(p -> bindings.setParent(p));

        if (scrElOpt.isPresent()) {
            var scrEl = scrElOpt.get();

            var locOpt = scrEl.getSourceLoc();
            if (locOpt.isEmpty()) {
                throw new IOException("No script file specified");
            }
            var loc = locOpt.get();
            var bytesOpt = docIo.getResource(loc);
            if (bytesOpt.isPresent()) {
                var b = bytesOpt.get();
                var str = new String(b, StandardCharsets.UTF_8);
                this.evalString(str, scrEl.getScriptLang(), bindings);
            } else {
                throw new IOException("Couldn't open script file");
            }
        } else {

        }
    }

    /**
     * Evaluate a string of code. Provided for testing.
     *
     * @param code Code to eval.
     * @param lang Language.
     * @param bindings Bindings to use.
     * @throws ScriptException Bad script.
     */
    protected void evalString(final String code, final String lang, final RecursiveBindings bindings) throws ScriptException {
        evalString(code, lang, bindings, defaultWriter);
    }

    /**
     * Evaluate a string in the top-level context.
     *
     * @param code Code to evaluate.
     * @param lang Language code is.
     * @throws ScriptException Bad code.
     */
    public void evalString(final String code, final String lang) throws ScriptException {
        evalString(code, lang, topLevelBindings);
    }

    /**
     * Evaluate a string in the given bindings, with the specified output.
     *
     * @param code Code to evaluate.
     * @param lang Language code is.
     * @param bindings Bindings to use.
     * @param outWriter Output Writer.
     * @throws ScriptException Bad code.
     */
    private void evalString(final String code, final String lang, final RecursiveBindings bindings, final Writer outWriter) throws ScriptException {
        var scrEngine = getScriptEngine(lang);
        scrEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        scrEngine.getContext().setWriter(outWriter);
        scrEngine.eval(code);
    }

    /**
     * Invoke function on element.
     *
     * @param element Element to start with.
     * @param function Function to try and call.
     * @param args Arguments to function.
     */
    public void invokeOnElement(final Scriptable element, final String function, final Object... args) throws ScriptException, IOException {
        if (element.getEvalRequired()){
            this.evalElement(element);
            element.setEvalRequired(false);
        }
        var scEng = getDefaultScriptEngine();
        scEng.setBindings(element.getScriptingBindings(), ScriptContext.ENGINE_SCOPE);
        try {
            ((Invocable) scEng).invokeFunction(function, args);
        } catch (ScriptException ex) {
            Logger.getLogger(Scripting.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            //Ignore nosuchmethod.
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
    public void execElementClick(final Scriptable element, final String button_name, final Double x_loc, final Double y_loc, final Boolean down) {

    }

    /**
     *
     * @param element Element to use.
     * @param keyName Key name.
     * @param ctrlDown Is ctrl down.
     * @param altDown Is alt down.
     * @param metaDown Is meta down.
     * @param keyDown Is the key down.
     */
    public void execElementKeyPress(final Scriptable element, final String keyName, final Boolean ctrlDown, final Boolean altDown, final Boolean metaDown, final Boolean keyDown) {
        var scEng = getDefaultScriptEngine();
        scEng.setBindings(element.getScriptingBindings(), ScriptContext.ENGINE_SCOPE);
        try {
            ((Invocable) scEng).invokeFunction(KEY_PRESS_FN, keyName, ctrlDown, altDown, metaDown, keyDown);
        } catch (ScriptException ex) {
            Logger.getLogger(Scripting.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            //Ignore nosuchmethod.
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

    private ScriptEngine getDefaultScriptEngine() {
        return getScriptEngine(defaultLang);
    }
}
