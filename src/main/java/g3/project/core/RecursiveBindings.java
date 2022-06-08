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

import javax.script.SimpleBindings;
import java.util.Optional;

/**
 * Recursive bindings will search current bindings, then parent bindings, etc,
 * etc, for the item.
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class RecursiveBindings extends SimpleBindings {

    /**
     * Parent bindings.
     */
    private Optional<RecursiveBindings> parentBindings = Optional.empty();

    /**
     * Constructor.
     */
    public RecursiveBindings() {
        super();
    }

    /**
     * Get the optional parent bindings.
     *
     * @return Optional parent.
     */
    public Optional<RecursiveBindings> getParent() {
        return this.parentBindings;
    }

    /**
     * Set parent bindings.
     *
     * @param p Parent bindings.
     */
    public void setParent(final RecursiveBindings p) {
        this.parentBindings = Optional.ofNullable(p);
    }

    /**
     * Do the local bindings contain the key?
     *
     * @param key Key to find.
     * @return True if key is contained.
     */
    public boolean localContainsKey(final Object key) {
        return super.containsKey(key);
    }

    /**
     * Get key in local bindings.
     *
     * @param key key to retrieve.
     * @return Optional object.
     */
    public Optional<Object> localGet(final Object key) {
        return Optional.ofNullable(super.get(key));
    }

    @Override
    public boolean containsKey(final Object key) {
        if (localContainsKey(key)) { //Do I have it?
            return true;
        } else {
            if (this.parentBindings.isPresent()) { //Search up through
                return this.parentBindings.get().containsKey(key);
            }
        }
        /*
         * I either don't have it,
         * or I have no parent and no-one else had it
         */
        return false;
    }

    @Override
    public Object get​(final Object key) {
        if (localGet(key).isPresent()) { //Do I have it?
            return localGet(key).get();
        } else {
            if (this.parentBindings.isPresent()) { //Search up through
                return this.parentBindings.get().get(key);
            }
        }
        //I have no parent and no-one else had it.
        return null;
    }
}
