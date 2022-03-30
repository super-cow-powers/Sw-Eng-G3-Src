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
package g3.project.elements;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Text;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
final public class ScriptElement extends Element implements Invocable{
    private ScriptEngine my_engine = null;
    private Invocable inv_engine = null;
    private final String myLang = "python";
    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    public ScriptElement(String name) {
        super(name);
    }

    public ScriptElement(String name, String uri) {
        super(name, uri);
    }

    public ScriptElement(Element element) {
        super(element);
    }

    public String getScriptText() {
        String scriptStr = "";
        for (int i = 0; i < this.getChildCount(); i++) {
            var child = this.getChild(i);
            if (child instanceof Text) {
                scriptStr = scriptStr.concat(child.getValue());
            } else {
                //not text
            }
        }
        return scriptStr;
    }
    public void setScriptString(String script) throws ScriptException{
        this.removeChildren();
        this.appendChild(script);
        if (my_engine != null){
           my_engine.eval(this.getScriptText());
        }
    }
    
    /**
     * Get the language used for the script
     * Currently only supporting Python
     * @return String of language name
     */
    public String getScriptLang(){
        return myLang;
    }
    
    /**
     * Not currently supported
     * @param lang language name string
     * @return set language name string
     */
    public String setScriptLang(String lang){
        return myLang;
    }
    
    /**
     * Set the element's script engine
     * @param newScriptEngine 
     */
    public void setScriptingEngine(ScriptEngine newScriptEngine) throws ScriptException{
        this.my_engine = newScriptEngine;
        my_engine.eval(this.getScriptText());
        inv_engine = (Invocable) my_engine;
    }

    @Override
    public Object invokeMethod(Object arg0, String arg1, Object... arg2) throws ScriptException, NoSuchMethodException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object invokeFunction(String func, Object... arg) throws ScriptException, NoSuchMethodException {
        return inv_engine.invokeFunction(func, arg);
    }

    @Override
    public <T> T getInterface(Class<T> arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T getInterface(Object arg0, Class<T> arg1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
