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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class Editor extends Stage {
//CHECKSTYLE:OFF

    private static final Double SCENE_WIDTH = 300.0;
    private static final Double SCENE_HEIGHT = 200.0;
//CHECKSTYLE:ON

    /**
     * Text edit area
     */
    private final TextArea editArea = new TextArea();
    /**
     * Box for controls.
     */
    private final HBox controlBox = new HBox();
    /**
     * Save button.
     */
    private final Button saveButton = new Button("Save");
    /**
     * Language select box.
     */
    private final ComboBox<String> languageSelectBox = new ComboBox<>(FXCollections.observableArrayList(
            "python",
            "JavaScript"
    ));
/**
 * Label for element ID.
 */
    private final Label elLabel = new Label();

    /**
     * Callback on save.
     */
    private final BiConsumer<String, String> saveCallback;

    /**
     * Create a new Console.
     *
     * @param ownerWindow Window that owns this.
     * @param text Text to be edited.
     * @param saveConsumer Consumer that takes the language name and edited text
     * on save.
     * @param elID Element ID.
     * @param lang Language.
     */
    public Editor(final Stage ownerWindow, final String text, final String lang, final String elID, final BiConsumer<String, String> saveConsumer) {
        this.initModality(Modality.NONE);
        this.initOwner(ownerWindow);
        VBox dialogVbox = new VBox();
        dialogVbox.getChildren().addAll(editArea, controlBox);
        controlBox.getChildren().addAll(saveButton, languageSelectBox, elLabel);
        elLabel.setText(elID);
        if (lang.toLowerCase().equals("rhino")) {
            languageSelectBox.setValue("JavaScript");
        } else {
            languageSelectBox.setValue(lang);
        }

        editArea.setText(text);
        dialogVbox.setVgrow(editArea, Priority.ALWAYS);
        Scene dialogScene = new Scene(dialogVbox, SCENE_WIDTH, SCENE_HEIGHT);
        this.setScene(dialogScene);
        this.saveCallback = saveConsumer;
        saveButton.setOnMouseClicked(e -> {
            String selLang = languageSelectBox.getValue();
            if (selLang.toLowerCase().equals("javascript")) { //force Rhino over nashorn.
                selLang = "rhino";
            }
            saveCallback.accept(selLang, editArea.getText());
        }); //Call the callback.
        this.show();
    }
}
