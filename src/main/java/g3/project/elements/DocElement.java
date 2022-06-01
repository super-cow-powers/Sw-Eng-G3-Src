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

import g3.project.core.RecursiveBindings;
import g3.project.graphics.SizeObj;
import java.io.IOException;
import java.nio.file.Path;
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
     * My script bindings.
     */
    private RecursiveBindings topLevelBindings = null;
    /**
     * My currently open page.
     */
    private PageElement currentPage = null;
    /**
     * Doc validation errors.
     */
    private ArrayList<String> validationErrors = new ArrayList<>();

    /**
     * Change callback.
     */
    private Consumer<VisualElement> updateCallback = (f) -> {
    };

    /**
     * Does the script need evaluating again?
     */
    private Boolean evalRequired = true;

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
     * Set doc validation errors.
     *
     * @param errors validation errors.
     */
    public void setValidationErrors(final ArrayList<String> errors) {
        validationErrors = errors;
    }

    /**
     * Get any validation errors in the document.
     *
     * @return Array of validation errors.
     */
    public ArrayList<String> getValidationErrors() {
        return validationErrors;
    }

    /**
     *
     * @return ArrayList containing the Doc's pages
     */
    public ArrayList<PageElement> getPages() {
        ArrayList<PageElement> pages = new ArrayList<>();
        for (int i = 0; i < this.getChildCount(); i++) {
            var node = this.getChild(i);
            if (node.getClass() == PageElement.class) {
                pages.add((PageElement) node);
            }
        }
        return pages;
    }

    /**
     * Get the currently open page.
     *
     * @return Maybe Page (if open).
     */
    public Optional<PageElement> getCurrentPage() {
        return Optional.ofNullable(currentPage);
    }

    /**
     * Maybe get target page.
     *
     * @param pageID Target page ID.
     * @return Maybe page.
     */
    public Optional<PageElement> getPage(final String pageID) {
        var pages = this.getPages();
        var it = pages.iterator();
        Integer ind = 0;
        while (it.hasNext()) {
            var page = it.next();
            var pgID = page.getID();
            if (pgID.equals(pageID)) {
                currentPage = page;
                page.setIndex(ind);
                return Optional.of(page);
            }
            ind ++;
        }
        return Optional.empty();
    }

    /**
     * Maybe get a target page.
     *
     * @param pageNum Target page number.
     * @return Maybe page.
     */
    public Optional<PageElement> getPage(final Integer pageNum) {
        var pages = this.getPages();
        try {
            PageElement page = pages.get(pageNum);
            page.setIndex(pageNum);
            currentPage = page;
            return Optional.of(page);
        } catch (IndexOutOfBoundsException ex) {
            return Optional.empty();
        }
    }

    /**
     * Add a new page.
     *
     * @param pageNum Page number.
     * @param el Page.
     */
    public void addPage(final Integer pageNum, final PageElement el) {
        for (int i = 0; i < this.getChildCount(); i++) {
            var node = this.getChild(i);
            if (node instanceof PageElement) {
                this.insertChild(el, i + pageNum);
            }
        }
    }

    /**
     * Add a page with the specified requirements.
     *
     * @param pageNum Page Number.
     * @param pageTitle Page Title/Friendly Name.
     * @param xSize X Size in PX.
     * @param ySize Y Size in PX.
     * @return New Page's ID.
     */
    public String addPage(final Integer pageNum, final String pageTitle, final Double xSize, final Double ySize) {
        String id = getNewUniqueID("page");
        PageElement el = new PageElement("base:page", VisualElement.BASE_URI);
        el.setID(id);
        addPage(pageNum, el);
        el.setTitle(pageTitle);
        el.setSize(new SizeObj(xSize, ySize, 0d));
        return id;
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
        elementScriptBindings.setParent(this.getParentElementScriptingBindings().get());
        return elementScriptBindings;
    }

    /**
     * Get the program-base definitions.
     *
     * @return Optional Bindings
     */
    @Override
    public Optional<RecursiveBindings> getParentElementScriptingBindings() {
        return Optional.of(topLevelBindings);
    }

    /**
     * Set the global/top-level bindings for scripting.
     *
     * @param bin Bindings.
     */
    public void setTopLevelBindings(final RecursiveBindings bin) {
        topLevelBindings = bin;
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
    public String getRealType() {
        return this.getClass().getName();
    }

    /**
     * Attach a new script to the element.
     *
     * @param path Internal path to file.
     * @param language Script language.
     */
    @Override
    public void addScriptFile(final Path path, final String language) throws IOException {
        if (!path.getFileSystem().provider().getScheme().contains("jar") && !path.getFileSystem().provider().getScheme().contains("zip")) {
            throw new IOException("External files not supported. Add the file to the project.");
        }
        ScriptElement scEl = new ScriptElement("ext:script", VisualElement.EXT_URI, path.toString(), language);
        var chEls = this.getChildElements();
        //Remove other scripts.
        for (var ch : chEls) {
            if (ch instanceof ScriptElement) {
                this.removeChild(ch);
            }
        }
        this.appendChild(scEl);
    }

    @Override
    public Boolean getEvalRequired() {
        return evalRequired;
    }

    @Override
    public void setEvalRequired(Boolean req) {
        evalRequired = req;
    }
}
