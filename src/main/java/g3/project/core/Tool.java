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

import g3.project.elements.ImageElement;
import g3.project.elements.ScriptElement;
import g3.project.elements.Scriptable;
import nu.xom.Builder;
import nu.xom.Element;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
<<<<<<< Updated upstream
 *
 * @author David Miall<dm1306@york.ac.uk>
=======
 * @author Group 3
>>>>>>> Stashed changes
 */
public class Tool extends Element implements Scriptable {

    /**
<<<<<<< Updated upstream
     * My script bindings.
     */
    RecursiveBindings elementScriptBindings = new RecursiveBindings();

    /**
=======
>>>>>>> Stashed changes
     * Create a builder.
     */
    private static final ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ToolsFactory());
        }

    };
    /**
     * Is Eval Required.
     */
    protected Boolean evalRequired = true;
    /**
     * My script bindings.
     */
    protected RecursiveBindings elementScriptBindings = new RecursiveBindings();

    /**
     * Constructor.
     *
     * @param name Tool name.
     */
    public Tool(final String name) {
        super(name);
    }

    /**
     * Constructor.
     *
     * @param name Tool name.
     * @param uri  Tool URI.
     */
    public Tool(final String name, final String uri) {
        super(name, uri);
    }

    /**
     * Constructor.
     *
     * @param element Tool Element.
     */
    public Tool(final Element element) {
        super(element);
    }

    /**
     * Get tool name.
     *
     * @return Tool name string.
     */
    public final String getName() {
        var name = this.getAttribute("name");
        return name.getValue();
    }

    /**
     * Get tool ID.
     *
     * @return Tool ID string.
     */
    public final String getID() {
        var id = this.getAttribute("ID");
        return id != null ? id.getValue() : "tool-null-id";
    }

    /**
     * Get the path to the associated image.
     *
     * @return Maybe path.
     */
    public final Optional<String> getImagePath() {
        var chEls = this.getChildElements();
        for (var ch : chEls) {
            if (ch instanceof ImageElement) {
                return ((ImageElement) ch).getSourceLoc();
            }
        }
        return Optional.empty();
    }

    /**
     * Get whether this tool sinks all events, or is pass-through.
     *
     * @return Is sink?
     */
    public final Boolean sinkEvents() {
        var sinkAttr = this.getAttribute("is_sink");
        if (sinkAttr == null) {
            return false;
        } else {
            return Boolean.valueOf(sinkAttr.getValue());
        }
    }

    /**
     * Get the local scope for this object.
     *
     * @return my Bindings.
     */
    @Override
    public final RecursiveBindings getScriptingBindings() {
        return elementScriptBindings;
    }

    /**
     * Return the Global bindings.
     *
     * @return Optional Bindings
     */
    @Override
    public final Optional<RecursiveBindings> getParentElementScriptingBindings() {
        return Optional.of(Scripting.getTopLevelBindings());
    }

    /**
     * Get the ScriptElement attached to this object. There should only be one
     * element.
     *
     * @return my (first) script element.
     */
    @Override
    public final Optional<ScriptElement> getScriptEl() {
        var chEls = this.getChildElements();
        for (var ch : chEls) {
            if (ch instanceof ScriptElement) {
                return Optional.of((ScriptElement) ch);
            }
        }
        return Optional.empty();
    }

    @Override
    public final String getRealType() {
        return this.getClass().getName();
    }

    /**
     * Do not support modifying tools file on the fly. Will always throw
     * IOException.
     *
     * @param path     path to file.
     * @param language Script language.
     * @throws IOException If called.
     */
    @Override
    public void addScriptFile(final Path path, final String language) throws IOException {
        throw new IOException("Editing tools not supported.");
    }

    /**
     * Evaluate only at load.
     *
     * @return False.
     */
    @Override
    public Boolean getEvalRequired() {
        return false;
    }

    /**
     * Set if I should be re-evaluated.
     *
     * @param req Re-eval?
     */
    @Override
    public void setEvalRequired(final Boolean req) {
    }
}
