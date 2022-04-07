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

import g3.project.core.Engine;
import g3.project.core.RecursiveBindings;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;
import nu.xom.*;

/**
 *
 * @author david
 */
public final class DocElement extends Element implements Scriptable {

    /**
     * Directory containing this document (String).
     */
    private String containingDirStr = null;

    /**
     * Change callback.
     */
    private Consumer<VisualElement> updateCallback = (f) -> {
    };

    /**
     * Script bindings for the element.
     */
    private RecursiveBindings elementScriptBindings = new RecursiveBindings();

//CHECKSTYLE:OFF
    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    public DocElement(final String name) {
        super(name);
    }

    public DocElement(final String name, String uri) {
        super(name, uri);
    }

    public DocElement(final Element element) {
        super(element);
    }

//CHECKSTYLE:ON
    /**
     * Set document base directory.
     *
     * @param dir Directory string.
     */
    public void setBaseDir(final String dir) {
        containingDirStr = dir;
    }

    /**
     * Set change callback.
     *
     * @param func Notifier Function.
     */
    public void setChangeCallback(final Consumer<VisualElement> func) {
        updateCallback = func;
    }

    /**
     * Get the change callback.
     *
     * @return change callback.
     */
    public Consumer<VisualElement> getChangeCallback() {
        return updateCallback;
    }

    /**
     * Get document base directory.
     *
     * @return Directory string.
     */
    public Optional<String> getBaseDir() {
        return Optional.ofNullable(containingDirStr);
    }

    /**
     *
     * @return ArrayList containing the Doc's pages
     */
    public Optional<ArrayList<PageElement>> getPages() {
        ArrayList<PageElement> pages = null;
        for (int i = 0; i < this.getChildCount(); i++) {
            var node = this.getChild(i);
            if (node.getClass() == PageElement.class) {
                if (pages == null) {
                    pages = new ArrayList<PageElement>();
                }

                pages.add((PageElement) node);
            }
        }
        return Optional.ofNullable(pages);
    }

    /**
     * Return a new Unique ID for an element. The param is not really necessary,
     * but useful
     *
     * @param idForType Type of object (will be prefixed to returned ID).
     * @return New UID
     */
    public String getNewUniqueID(final String idForType) {
        Random rand = new Random();
        long time = System.currentTimeMillis();
        var timestr = String.valueOf(time);
        var idStr = idForType.concat(timestr);
        while (!validateUniqueID(idStr)) {
            //CHECKSTYLE:OFF
            time = System.currentTimeMillis() - rand.nextInt(200000);
            //CHECKSTYLE:ON
            timestr = String.valueOf(time);
            idStr = idForType.concat(timestr);
        }
        return idStr;
    }

    /**
     * Validates if the given ID is unique or not. The element it will be
     * attached to must NOT already be in the doc!
     *
     * @param id ID to validate.
     * @return Validity
     */
    public Boolean validateUniqueID(final String id) {
        return getElementByID(id).isEmpty();
    }

    /**
     * Returns an element in the doc by its' ID.
     *
     * @param id ID of element.
     * @return Optional of Element
     */
    public Optional<VisualElement> getElementByID(final String id) {
        /*
        @todo: Make faster
         */
        for (var el : this.getChildElements()) {
            if (el instanceof VisualElement) {
                var elOp = ((VisualElement) el).getByID(id);
                if (elOp.isPresent()) {
                    return elOp; //Found it
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public RecursiveBindings getScriptingBindings() {
        return elementScriptBindings;
    }

    /**
     * Get Local Script Bindings of parent node, if parent node is another
     * Scriptable element.
     *
     * @return Optional Bindings
     */
    @Override
    public Optional<RecursiveBindings> getParentElementScriptingBindings() {
        var parent = this.getParent();
        if (parent instanceof Scriptable) {
            return Optional.of(((Scriptable) parent).getScriptingBindings());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get the ScriptElement attached to this object. There should only be one
     * element.
     *
     * @return my (first) script element.
     */
    @Override
    public Optional<ScriptElement> getScriptEl() {
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
}
