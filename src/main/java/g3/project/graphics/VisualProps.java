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

import java.util.HashMap;
import javafx.scene.paint.Color;
import java.lang.reflect.Type;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;
import javafx.scene.effect.DropShadow;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class VisualProps extends HashMap<String, Object> {

    /*
<xsd:attribute name="shade_colour" type="base:colourType" use="optional"/>
<xsd:attribute name="l-shade-px" type="xsd:double" use="optional"/>
<xsd:attribute name="r-shade-px" type="xsd:double" use="optional"/>
<xsd:attribute name="t-shade-px" type="xsd:double" use="optional"/>
<xsd:attribute name="b-shade-px" type="xsd:double" use="optional"/>
<xsd:attribute name="alpha" type="base:percentType" use="optional" />
<xsd:attribute name="fill" type="base:colourType" use="optional" />
<xsd:attribute name="disp_duration_s" type="xsd:double" use="optional" />
<xsd:attribute name="show_after_s" type="xsd:double" use="optional" />
<xsd:anyAttribute /> <!-- permit any valid attribute -->
     */
    //CHECKSTYLE:OFF
    protected static final String SHADE_COL = "shade_colour";
    protected static final Class SHADE_COL_TYPE = Color.class;
    protected static final Class SHADE_SIZE_TYPE = Double.class;
    protected static final String L_SHADE_SIZE = "l-shade-px";
    protected static final String R_SHADE_SIZE = "r-shade-px";
    protected static final String T_SHADE_SIZE = "t-shade-px";
    protected static final String B_SHADE_SIZE = "b-shade-px";
    protected static final String ALPHA = "alpha";
    protected static final Class ALPHA_TYPE = Double.class;
    protected static final String FILL = "fill";
    protected static final Class FILL_TYPE = Color.class;
    protected static final String DISP_SECS = "disp_duration_s";
    protected static final Class DISP_SECS_TYPE = Double.class;
    protected static final String DELAY_SECS = "show_after_s";
    protected static final Class DELAY_SECS_TYPE = Double.class;
    protected static final String VISIBLE = "visible";
    protected static final Class VISIBLE_TYPE = Boolean.class;
    protected static final String SIZE = "size";
    protected static final Class SIZE_TYPE = SizeObj.class;
    protected static final String LOCATION = "loc";
    protected static final Class LOCATION_TYPE = LocObj.class;
    //CHECKSTYLE:ON
    /**
     * Contains known props and their classes.
     */
    public static final Map<String, Class> PROPS_MAP = Map.ofEntries(entry(SHADE_COL, SHADE_COL_TYPE),
            entry(L_SHADE_SIZE, SHADE_SIZE_TYPE), entry(R_SHADE_SIZE, SHADE_SIZE_TYPE), entry(T_SHADE_SIZE, SHADE_SIZE_TYPE), entry(B_SHADE_SIZE, SHADE_SIZE_TYPE),
            entry(ALPHA, ALPHA_TYPE), entry(FILL, FILL_TYPE), entry(DISP_SECS, DISP_SECS_TYPE),
            entry(DELAY_SECS, DELAY_SECS_TYPE), entry(VISIBLE, VISIBLE_TYPE), entry(LOCATION, LOCATION_TYPE), entry(SIZE, SIZE_TYPE));
    /**
     * Contains default values for known props.
     */
    public static final Map<String, Object> PROP_DEFAULTS = Map.ofEntries(entry(SHADE_COL, Color.BLACK),
            entry(L_SHADE_SIZE, 0), entry(R_SHADE_SIZE, 0), entry(T_SHADE_SIZE, 0), entry(B_SHADE_SIZE, 0),
            entry(ALPHA, 0), entry(FILL, Color.TRANSPARENT), entry(DISP_SECS, -1), entry(DELAY_SECS, 0), entry(VISIBLE, true),
            entry(LOCATION, new LocObj(Point2D.ZERO, 0d)), entry(SIZE, new SizeObj(0d, 0d, 0d)));
    /**
     * Contains CSS strings for known CSS props.
     */
    private static final Map<String, String> CSS = Map.ofEntries(entry(FILL, "-fx-fill: %s;"), entry(VISIBLE, "visibility: %s"));

    /**
     * Constructor. Takes map of properties.
     *
     * @param propertiesMap Map of properties conforming to PROPS_MAP.
     */
    public VisualProps(final Map<String, Object> propertiesMap) {
        super();
        //Check for all known properties.
        for (final String propKey : PROPS_MAP.keySet()) {
            var propValMaybe = Optional.ofNullable(propertiesMap.get(propKey));
            propValMaybe.ifPresent(v -> {
                this.put(propKey, v);
            });
        }
    }

    /**
     * Empty constructor.
     */
    public VisualProps() {
        super();
    }

    /**
     * Return value of specified property.
     *
     * @param prop Property to get.
     * @return Maybe property. Empty if invalid.
     */
    public final Optional<Object> getProp(final String prop) {
        var val = super.get(prop);
        if (val == null) { //Not Found. Set default.
            val = PROP_DEFAULTS.get(prop);
        }
        //Will be null if invalid. Return optional to limit damage.
        return Optional.ofNullable(val);
    }

    /**
     * Maybe get a shadow for the item.
     *
     * @return Maybe shadow.
     */
    public final Optional<DropShadow> makeShadow() {
        var l = (Double) this.getProp(L_SHADE_SIZE).get();
        var r = (Double) this.getProp(R_SHADE_SIZE).get();
        var t = (Double) this.getProp(T_SHADE_SIZE).get();
        var b = (Double) this.getProp(B_SHADE_SIZE).get();
        var width = l + r;
        var height = t + b;
        if ((width + height) == 0) { //No shadow set.
            return Optional.empty();
        }

        var xOS = r - l;
        var yOS = b - t;
        var ds = new DropShadow();
        ds.setHeight(height);
        ds.setWidth(width);
        ds.setOffsetX(xOS);
        ds.setOffsetY(yOS);

        return Optional.of(ds);
    }

    /**
     * Get the JFX CSS for these properties.
     *
     * @return CSS String.
     */
    public final String toCSS() {
        var propsStream = CSS.keySet().stream();
        return propsStream.map(p -> {
            var cssFmt = CSS.get(p);
            Object val = this.getProp(p).get();
            switch (p) { //These aren't the same types in JFX css as in our files.
                case FILL: //Fill may have alpha included or use the alpha prop.
                    var alpha = (Double) this.get(ALPHA);
                    if (alpha != null) {
                        var cVal = (Color) val;
                        val = new Color(cVal.getRed(), cVal.getGreen(), cVal.getBlue(), alpha);
                    }
                    break;
                default:
                    break;
            }
            return String.format(cssFmt, val.toString());
        }).collect(Collectors.joining(" "));
    }

}
