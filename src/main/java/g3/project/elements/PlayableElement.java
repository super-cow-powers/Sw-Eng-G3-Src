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
import nu.xom.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class PlayableElement extends VisualElement implements Includable {
//CHECKSTYLE:OFF

    /*
     Attributes
     */
    private static final String AUTOPLAY = "autoplay";
    private static final String LOOP = "loop";
    private static final String OFFSET = "seek_offset";
    private static final String DISP_PLAYER = "display_player";
//CHECKSTYLE:ON
    private static ThreadLocal builders = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new Builder(new ElementFactory());
        }

    };

    public PlayableElement(String name) {
        super(name);
    }

    public PlayableElement(String name, String uri) {
        super(name, uri);
    }

    public PlayableElement(Element element) {
        super(element);
    }

    /**
     * Should the player start automatically?
     *
     * @return Auto-play.
     */
    public Boolean getAutoplay() {
        var autoAttr = this.getAttribute(AUTOPLAY);
        if (autoAttr != null) {
            var autoStr = autoAttr.getValue();
            try {
                return Boolean.valueOf(autoStr);
            } catch (Exception ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Set auto-play.
     *
     * @param auto Auto-play.
     */
    public void setAutoplay(final Boolean auto) {
        var autoAttr = new Attribute(AUTOPLAY, auto.toString());
        this.addAttribute(autoAttr);
    }

    /**
     * Should a player be displayed?
     *
     * @return display player.
     */
    public Boolean getDisplayPlayer() {
        var dispAttr = this.getAttribute(DISP_PLAYER);
        if (dispAttr != null) {
            var dispStr = dispAttr.getValue();
            try {
                return Boolean.valueOf(dispStr);
            } catch (Exception ex) {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * Set display of player.
     *
     * @param disp player display.
     */
    public void setDisplayPlayer(final Boolean disp) {
        var dispAttr = new Attribute(DISP_PLAYER, disp.toString());
        this.addAttribute(dispAttr);
    }

    /**
     * Get the seek offset for the player (in seconds).
     *
     * @return Seek offset.
     */
    public Double getSeekOffset() {
        var seekAttr = this.getAttribute(OFFSET);
        if (seekAttr != null) {
            var osStr = seekAttr.getValue();
            try {
                return Double.valueOf(osStr);
            } catch (Exception ex) {
                return 0d;
            }
        } else {
            return 0d;
        }
    }

    /**
     * Set the Seek offset (in seconds).
     *
     * @param offset seek offset.
     */
    public void setSeekOffset(final Double offset) {
        var seekAttr = new Attribute(OFFSET, offset.toString());
        this.addAttribute(seekAttr);
    }

    /**
     * Should play looped?
     *
     * @return loop.
     */
    public Boolean getLoop() {
        var loopAttr = this.getAttribute(LOOP);
        if (loopAttr != null) {
            var loopStr = loopAttr.getValue();
            try {
                return Boolean.valueOf(loopStr);
            } catch (Exception ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Set if play should loop.
     *
     * @param loop loop play.
     */
    public void setLoop(final Boolean loop) {
        var loopAttr = new Attribute(LOOP, loop.toString());
        this.addAttribute(loopAttr);
    }

    @Override
    public Optional<String> getSourceLoc() {
        return Optional.ofNullable(this.getAttribute(INCLUDE_ATTR))
                .map(f -> f.getValue());
    }

}
