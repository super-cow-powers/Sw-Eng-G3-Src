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

import java.util.Optional;
import java.util.ArrayList;
import java.util.Random;
import nu.xom.*;

/**
 *
 * @author david
 */
public class DocElement extends Element {

    private String containing_dir = null;

    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    public DocElement(String name) {
        super(name);
    }

    public DocElement(String name, String uri) {
        super(name, uri);
    }

    public DocElement(Element element) {
        super(element);
    }

    public void SetBaseDir(String dir) {
        containing_dir = dir;
    }

    public Optional<String> GetBaseDir() {
        return Optional.ofNullable(containing_dir);
    }

    /**
     *
     * @return Returns vector containing the Doc's pages
     */
    public Optional<ArrayList<PageElement>> GetPages() {
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
     * @param idForType
     * @return New UID
     */
    public String NewUniqueID(String idForType) {
        Random rand = new Random();
        long time = System.currentTimeMillis();
        var timestr = String.valueOf(time);
        var id_str = idForType.concat(timestr);
        while (ValidateUniqueID(id_str) == false) {
            time = System.currentTimeMillis() - rand.nextInt(200000);
            timestr = String.valueOf(time);
            id_str = idForType.concat(timestr);
        }
        return id_str;
    }

    /**
     * Validates if the given ID is unique or not
     *
     * @param ID
     * @return
     */
    public Boolean ValidateUniqueID(String ID) {

        return true;
    }

}
