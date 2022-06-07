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

import java.util.HashMap;
import javafx.scene.paint.Color;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Group 3
 */
public final class StrokeProps extends HashMap<String, Object> implements Props {

    /*
<xsd:attribute name="width" type="xsd:double" />
<xsd:attribute name="dash-style" type="xsd:string">
<xsd:attribute name="colour" type="base:colourType" />
     */
    //CHECKSTYLE:OFF
    public static final String WIDTH = "width";
    public static final Class WIDTH_TYPE = Double.class;
    public static final String LINE_STYLE = "dash-style";
    public static final Class LINE_STYLE_TYPE = String.class;
    public static final String COLOUR = "colour";
    public static final Class COLOUR_TYPE = Color.class;
    public static final String LINE_CAP = "line-cap";
    public static final Class LINE_CAP_TYPE = String.class;

    private static final String DOT_DASH_ARRAY = "12 2 4 2";
    private static final String DASH_ARRAY = "12 12";
    private static final String DOT_ARRAY = "2 2";
    public static final String DOT_DASH_STYLE = "dot-dash";
    public static final String DASH_STYLE = "dash";
    public static final String DOT_STYLE = "dot";
    public static final String SOLID_STYLE = "plain";
    //CHECKSTYLE:ON
    /**
     * Contains known props and their classes.
     */
    protected static final Map<String, Class> PROPS_MAP = Map.ofEntries(entry(WIDTH, WIDTH_TYPE),
            entry(LINE_STYLE, LINE_STYLE_TYPE), entry(COLOUR, COLOUR_TYPE), entry(LINE_CAP, LINE_CAP_TYPE));
    /**
     * Contains default values for known props.
     */
    protected static final Map<String, Object> PROP_DEFAULTS = Map.ofEntries(entry(WIDTH, 0d),
            entry(LINE_STYLE, SOLID_STYLE), entry(COLOUR, Color.TRANSPARENT), entry(LINE_CAP, "butt"));

    /**
     * Property friendly names.
     */
    protected static final Map<String, String> PROPS_NAMES = Map.ofEntries(entry(WIDTH, "Width"),
            entry(LINE_STYLE, "Stroke Style"), entry(COLOUR, "Stroke Colour"), entry(LINE_CAP, "Stroke Cap"));
    /**
     * Contains CSS strings for known props.
     */
    private static final Map<String, String> CSS = Map.ofEntries(entry(WIDTH, "-fx-stroke-width: %s; "),
            entry(LINE_STYLE, "-fx-stroke-dash-array: %s;"), entry(COLOUR, "-fx-stroke: \'%s\';"), entry(LINE_CAP, "-fx-stroke-line-cap: %s;"));

    /**
     * Constructor. Takes map of properties.
     *
     * @param propertiesMap Map of properties conforming to PROPS_MAP.
     */
    public StrokeProps(final Map<String, Object> propertiesMap) {
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
    public StrokeProps() {
        super();
    }

    /**
     * Return value of specified property.
     *
     * @param prop Property to get.
     * @return Maybe property. Empty if invalid.
     */
    @Override
    public Optional<Object> getProp(final String prop) {
        var val = super.get(prop);
        if (val == null) { //Not Found. Set default.
            val = PROP_DEFAULTS.get(prop);
        }
        switch (prop) { //These aren't the same types in JFX css as in our files.
            case LINE_STYLE:
                switch (((String) val).toLowerCase()) {
                    case SOLID_STYLE:
                        break;
                    case DOT_STYLE:
                        val = DOT_ARRAY;
                        break;
                    case DASH_STYLE:
                        val = DASH_ARRAY;
                        break;
                    case DOT_DASH_STYLE:
                        val = DOT_DASH_ARRAY;
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        //Might be null if invalid. Return optional to limit damage
        return Optional.ofNullable(val);
    }

    /**
     * Get the JFX CSS for these properties.
     *
     * @return CSS String.
     */
    public String toCSS() {
        var propsStream = PROPS_MAP.keySet().stream();
        var props = propsStream.map(p -> {
            var cssFmt = CSS.get(p);
            Object val = this.getProp(p).get();
            //Return nothing for solid stroke.
            return (val.toString().equals(SOLID_STYLE)) ? "" : String.format(cssFmt, val.toString());
        }).collect(Collectors.joining(" "));
        System.out.println(props);
        return props;
    }

    @Override
    public Map<String, Object> getDefaultProps() {
        return PROP_DEFAULTS;
    }

    @Override
    public Map<String, Class> getPropsTypes() {
        return PROPS_MAP;
    }

    @Override
    public Map<String, String> getPropsNames() {
        return PROPS_NAMES;
    }

    @Override
    public Map<String, Object> getProps() {
        return this;
    }

}
