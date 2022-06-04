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
package g3.project.graphics;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class SizeObj {
//CHECKSTYLE:OFF

    private final Double x_px;
    private final Double y_px;
    private final Double rot_deg;
//CHECKSTYLE:ON

    /**
     *
     * Create new object.X and Y are in PX, rot is in degrees.
     *
     * @param x Size in X.
     * @param y Size in Y.
     * @param rot Rotation angle in degrees.
     */
    public SizeObj(final Double x, final Double y, final Double rot) {
        this.x_px = x;
        this.y_px = y;
        this.rot_deg = rot % 360; //Mod 360 degrees.
    }

    /**
     * Get X Size.
     *
     * @return Returns X size in PX.
     */
    public Double getX() {
        return x_px;
    }

    /**
     * Get Y size.
     *
     * @return Returns Y size in PX
     */
    public Double getY() {
        return y_px;
    }

    /**
     * Get Rotation.
     *
     * @return Returns Rotation in Degrees
     */
    public Double getRot() {
        return rot_deg;
    }
}
