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
public interface Scriptable {

    /**
     * Get the local scope for this object.
     *
     * @return my Bindings.
     */
    RecursiveBindings getScriptingBindings();

    /**
     * Get Local Script Bindings of parent node, if parent node is another
     * Scriptable element.
     *
     * @return Optional Bindings
     */
    Optional<RecursiveBindings> getParentElementScriptingBindings();

    /**
     * Get the ScriptElement attached to this object. There should only be one
     * element.
     *
     * @return my (first) script element.
     */
    Optional<ScriptElement> getScriptEl();
    
    /**
     * Attach a script file to this object.
<<<<<<< Updated upstream
     * @param path Path to file.
=======
     *
     * @param path     Path to file.
>>>>>>> Stashed changes
     * @param language Script language.
     */
    void addScriptFile(Path path, String language) throws IOException;

    /**
     * Return the type of the element.
     *
     * @return Type-name
     */
    String getRealType();
    /**
     * Get if the element requires evaluating again.
     * @return 
     */
    Boolean getEvalRequired();
    /**
     * Set if the element requires evaluating again.
     * @param req Re-Evaluate is required?
     */
    void setEvalRequired(Boolean req);

}
