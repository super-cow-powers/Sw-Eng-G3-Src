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
package g3.project.ui;

import java.util.Optional;
import javafx.geometry.Point2D;

/**
 *
 * @author david
 */
public class LocObj {

    private final Point2D start;
    private final Point2D centre;
    private final Point2D end;
    private final Double zIndex;

    /**
     * Create location container. Any argument may be null
     *
     * @param startPoint
     * @param centrePoint
     * @param endPoint
     */
    public LocObj(Point2D startPoint, Point2D centrePoint, Point2D endPoint, Double z) {
        start = startPoint;
        centre = centrePoint;
        end = endPoint;
        zIndex = z != null ? z : 0;
    }

    public Optional<Point2D> getStart() {
        return Optional.ofNullable(start);
    }

    public Optional<Point2D> getCentre() {
        return Optional.ofNullable(centre);
    }

    public Optional<Point2D> getEnd() {
        return Optional.ofNullable(end);
    }

    public Double getZ() {
        return zIndex;
    }

}