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
package g3.project.ui;

import g3.project.graphics.SizeObj;
import g3.project.graphics.VisualProps;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Group 3
 */
public class VisImageView extends ImageView implements Visual {

    /**
     * Constructor.
     */
    public VisImageView() {
    }

    /**
     * Constructor.
     *
     * @param url URL to Image.
     */
    public VisImageView(final String url) {
        super(url);
    }

    /**
     * Constructor.
     *
     * @param image Image to show.
     */
    public VisImageView(final Image image) {
        super(image);
    }

    /**
     * Set Visual Properties.
     *
     * @param props Properties map.
     */
    @Override
    public void setVisualProps(final VisualProps props) {
        var shad = props.makeShadow();
        shad.ifPresent(sh -> this.setEffect(sh));
        var vis = props.getProp(VisualProps.VISIBLE);
        vis.ifPresent(vi -> this.setVisible((Boolean) vi));
        var alpha = props.getProp(VisualProps.ALPHA);
        alpha.ifPresent(a -> this.setOpacity((Double) a));
    }

    /**
     * Set Size.
     *
     * @param size Size.
     */
    @Override
    public void setSize(final SizeObj size) {
        this.fitWidthProperty().set(size.getX());
        this.fitHeightProperty().set(size.getY());
        this.setRotate(size.getRot());
    }
}
