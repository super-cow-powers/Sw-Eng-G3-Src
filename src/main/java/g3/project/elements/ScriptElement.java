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
package g3.project.elements;

import java.util.Optional;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Element;

/**
 *
 * @author Group 3
 */
public final class ScriptElement extends Element implements Includable {

    /**
     * Default language.
     */
    private static final String DEF_LANG = "python";

    /**
     * Language attribute.
     */
    private static final String LANG_ATTR = "language";

    /**
     * Script engine to run element's script.
     *
     * WARNING: This is a global, shared object!!!
     */
    private ScriptEngine globalScriptEngine = null;
    /**
     * Invocable form of script engine.
     */
    private Invocable invScriptEngine = null;
    /**
     * Script's language.
     */
    private final String myLang = "python";

    /**
     * Script is inline?
     */
    private Boolean inlineScript;

    //CHECKSTYLE:OFF
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
//CHECKSTYLE:ON

    /**
     * Constructor with script.
     *
     * @param name Element name.
     * @param uri Element URI.
     * @param scriptPath Path to script.
     * @param scriptLang Script Language.
     */
    public ScriptElement(final String name, final String uri, final String scriptPath, final String scriptLang) {
        super(name, uri);
        var sourceAttr = new Attribute(INCLUDE_ATTR, scriptPath);
        this.addAttribute(sourceAttr);
        this.setScriptLang(scriptLang);
        this.inlineScript = false;
    }

    /**
     * Get the language used for the script Currently only supporting Python.
     *
     * @return String of language name
     */
    public String getScriptLang() {
        var lang = this.getAttribute(LANG_ATTR);
        if (lang == null) {
            setScriptLang(DEF_LANG);
            return DEF_LANG;
        }
        return lang.getValue();
    }

    /**
     * Not currently supported.
     *
     * @param lang language name string
     */
    public void setScriptLang(final String lang) {
        var langAttr = new Attribute(LANG_ATTR, lang);
        this.addAttribute(langAttr);
    }

    /**
     * Get the script's source path or URL.
     *
     * @return Location string.
     */
    @Override
    public Optional<String> getSourceLoc() {
        //Get include_source attribute
        return Optional.ofNullable(this.getAttribute(INCLUDE_ATTR))
                .map(f -> f.getValue());
    }
}
