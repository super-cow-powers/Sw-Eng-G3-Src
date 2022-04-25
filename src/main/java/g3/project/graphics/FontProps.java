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

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public class FontProps extends HashMap<String, Object> {

    /*
    <xsd:attribute name="underscore" type="xsd:boolean" use="optional" />
					<xsd:attribute name="italic" type="xsd:boolean" use="optional" />
					<xsd:attribute name="bold" type="xsd:boolean" use="optional" />
					<xsd:attribute name="size" type="xsd:double" use="optional" />
					<xsd:attribute name="name" type="xsd:token" use="optional" />
					<xsd:attribute name="colour" type="base:colourType" use="optional" />
     */
    //CHECKSTYLE:OFF
    protected static final String US = "underscore";
    protected static final Class US_TYPE = Boolean.class;
    protected static final String IT = "italic";
    protected static final Class IT_TYPE = Boolean.class;
    protected static final String BOLD = "bold";
    protected static final Class BOLD_TYPE = Boolean.class;
    protected static final String SIZE = "size";
    protected static final Class SIZE_TYPE = Double.class;
    protected static final String FONT = "name";
    protected static final Class FONT_TYPE = String.class;
    protected static final String COLOUR = "colour";
    protected static final Class COLOUR_TYPE = Color.class;
    //CHECKSTYLE:ON
    /**
     * Contains known props and their classes.
     */
    public static final Map<String, Class> PROPS_MAP = Map.ofEntries(entry(US, US_TYPE),
            entry(IT, IT_TYPE), entry(BOLD, BOLD_TYPE), entry(SIZE, SIZE_TYPE), entry(FONT, FONT_TYPE), entry(COLOUR, COLOUR_TYPE));
    /**
     * Contains default values for known props.
     */
    public static final Map<String, Object> PROP_DEFAULTS = Map.ofEntries(entry(US, false), entry(IT, false), entry(BOLD, false),
            entry(SIZE, 16d), entry(FONT, "monospace"), entry(COLOUR, Color.BLACK));
    /**
     * Contains CSS strings for known props.
     */
    private static final Map<String, String> CSS = Map.ofEntries(entry(US, "-fx-underline: \'%s\';"), entry(IT, "-fx-font-style: \'%s\';"), entry(BOLD, "-fx-font-weight: \'%s\';"),
            entry(SIZE, "-fx-font-size: %s;"), entry(FONT, "-fx-font-family: \'%s\'"), entry(COLOUR, "-fx-fill: \'%s\'"));

    /**
     * Constructor. Takes map of properties.
     *
     * @param propertiesMap Map of properties conforming to PROPS_MAP.
     */
    public FontProps(final Map<String, Object> propertiesMap) {
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
    public FontProps(){
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
        //Might be null if invalid. Return optional to limit damage
        return Optional.ofNullable(val);
    }
    /**
     * Get the JFX CSS for these properties.
     * @return CSS String.
     */
    public String toCSS() {
        var propsStream = PROPS_MAP.keySet().stream();
        return propsStream.map(p -> {
            var cssFmt = CSS.get(p);
            Object val = getProp(p).get();
            switch (p) { //These aren't the same types in JFX css as in our files.
                case IT:
                    val = (Boolean) val == false ? "normal" : "italic";
                case BOLD:
                    val = (Boolean) val == false ? "normal" : "bold";
                default:
            }
            return String.format(cssFmt, val.toString());
        }).collect(Collectors.joining(" "));
    }

}
