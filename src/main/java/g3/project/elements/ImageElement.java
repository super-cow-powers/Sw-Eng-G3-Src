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

import java.net.URI;
import java.util.Optional;
import nu.xom.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class ImageElement extends VisualElement {

    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    public ImageElement(String name) {
        super(name);
    }

    public ImageElement(String name, String uri) {
        super(name, uri);
    }

    public ImageElement(Element element) {
        super(element);
    }

    public Optional<String> getSourceLoc() {

        return Optional.ofNullable(this.getAttribute("include_source")) //Get include_source attribute
                .map(f -> f.getValue())
                .map(f -> {
                    var base_doc = this.getDocument().getRootElement();
                    String my_dir = "";

                    if (base_doc instanceof DocElement) {
                        if (((DocElement) base_doc).GetBaseDir().isPresent()) {
                            my_dir = ((DocElement) base_doc).GetBaseDir().get();
                        }
                    }
                    String loc = null;

                    if (f.contains(":/") || f.startsWith("/")) {//Absolute Path
                        loc = f;
                    } else if (f.startsWith(".")) {//Relative Path
                        loc = my_dir.concat(f);
                    }
                    if (!f.startsWith("http")){
                        loc = "file:".concat(loc);
                    }
                    return loc;
                });

    }

}
