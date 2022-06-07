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
package g3.project.graphics;

/**
 *
 * @author Group 3
 */
public class StyledTextSeg {

    /**
     * Style.
     */
    private final FontProps style;
    /**
     * Text.
     */
    private final String text;
    /**
     * href properties, if I am an href.
     */
    private RefProps myRef = null;
    /**
     * Function to run on click of link.
     */
    private Runnable refAction = () -> {
        System.err.println(this.getRefTarget());
    };

    /**
     * Constructor. Create styled segment of text.
     *
     * @param myStyle Text Style.
     * @param myWord Text to style.
     */
    public StyledTextSeg(final FontProps myStyle, final String myWord) {
        style = myStyle;
        text = myWord;
    }

    /**
     * Set an href on this segment.
     *
     * @param target href target.
     * @param type href target type.
     */
    public final void setHRef(final String target, final REF_TYPE type) {
        myRef = new RefProps(type, target);
    }

    /**
     * Is this an href?
     *
     * @return Boolean.
     */
    public final Boolean isHref() {
        return myRef == null ? false : true;
    }

    /**
     * Get ref target, if it exists. THIS CAN RETURN NULL! ALWAYS CHECK REF
     * STATE FIRST.
     *
     * @return ref target. Will be null if not a ref.
     */
    public final String getRefTarget() {
        return myRef == null ? null : myRef.refTarget;
    }

    /**
     * Get ref type, if it exists. THIS CAN RETURN NULL! ALWAYS CHECK REF STATE
     * FIRST.
     *
     * @return ref type - internal or external. Will be null if not a ref.
     */
    public final REF_TYPE getRefType() {
        return myRef == null ? null : myRef.refType;
    }

    /**
     * Get the style.
     *
     * @return FontProps.
     */
    public final FontProps getStyle() {
        return style;
    }

    /**
     * Get the text segment.
     *
     * @return String.
     */
    public final String getString() {
        return text;
    }

    //CHECKSTYLE:OFF
    /**
     * Enumerator for use with refs. Is the ref to an internal or external
     * location?
     */
    public static enum REF_TYPE {
        INTERNAL,
        EXTERNAL,
    }

    /**
     * Properties for any ref.
     */
    private class RefProps {

        private final REF_TYPE refType;
        private final String refTarget;

        public RefProps(REF_TYPE type, String target) {
            refTarget = target;
            refType = type;
        }

        public String getTarget() {
            return refTarget;
        }

        public REF_TYPE getType() {
            return refType;
        }

    }
    //CHECKSTYLE:ON
}
