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

import java.util.function.Consumer;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Group 3
 */
public final class Console extends Stage {
//CHECKSTYLE:OFF

    private static final Double MAX_WIDTH = 40.0;
    private static final Double SCENE_WIDTH = 300.0;
    private static final Double SCENE_HEIGHT = 200.0;
//CHECKSTYLE:ON
    /**
     * Text input field
     */
    protected final TextField inputField = new TextField();
    /**
     * Text history
     */
    protected final TextArea historyArea = new TextArea();
    /**
     * Callback on line entered.
     */
    private final Consumer<String> lineCallback;

    /**
     * Create a new Console.
     *
     * @param ownerWindow Window that owns this.
     * @param lineEnterConsumer Callback on line entered.
     */
    public Console(final Stage ownerWindow, final Consumer<String> lineEnterConsumer) {
        this.initModality(Modality.NONE);
        if (ownerWindow != null) {
            this.initOwner(ownerWindow);
        }
        VBox dialogVbox = new VBox();
        Separator separator = new Separator();
        separator.setMaxWidth(MAX_WIDTH);
        separator.setOrientation(Orientation.HORIZONTAL);
        dialogVbox.getChildren().addAll(historyArea, separator, inputField);
        dialogVbox.setVgrow(historyArea, Priority.ALWAYS);
        historyArea.setEditable(false);
        historyArea.setFocusTraversable(false);
        Scene dialogScene = new Scene(dialogVbox, SCENE_WIDTH, SCENE_HEIGHT);
        this.setScene(dialogScene);
        this.lineCallback = lineEnterConsumer;

        inputField.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ENTER) {
                var line = inputField.getText().concat("\n");
                lineCallback.accept(line);
                historyArea.appendText(line);
                inputField.clear();
            }
        });
    }

    /**
     * Put a message to the console.
     *
     * @param message Message to show.
     */
    public void putMessage(final String message) {
        if (!message.endsWith("\n")) {
            historyArea.appendText(message.concat("\n"));
        } else {
            historyArea.appendText(message);
        }
    }

}
