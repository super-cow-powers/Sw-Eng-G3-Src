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
package g3.project.playable;

import g3.project.graphics.SizeObj;
import g3.project.graphics.VisualProps;
import g3.project.ui.Visual;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.State;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

/**
 *
 * @author Group 3
 *
 * Based on:
 * https://github.com/caprica/vlcj-javafx-demo/commit/a057335a1a0ad5761e6d78c469afe1b5a80a9f86
 */
public final class Player extends Group implements Visual {
//CHECKSTYLE:OFF

    private static final int MAX_VOL = 100;
    private static final double CTRL_MAX_HEIGHT = 50d;
    private final MediaPlayerFactory mediaPlayerFactory;

    private final EmbeddedMediaPlayer embeddedMediaPlayer;

    private final VBox playerVbox = new VBox();

    private final HBox controlHbox = new HBox();

    private final ImageView videoImageView;

    private WritableImage videoImage;

    private PixelBuffer<ByteBuffer> videoPixelBuffer;
    /**
     * Target width.
     */
    private final DoubleProperty targetWidth = new SimpleDoubleProperty(0);
    /**
     * Target height.
     */
    private final DoubleProperty targetHeight = new SimpleDoubleProperty(0);

    private Slider controlSlider = new Slider();
    private Slider volSlider = new Slider(0, MAX_VOL, 0);
    private Label volLabel = new Label(" 🔊");
    private Label timeLabel = new Label();
    private Button playPauseButton = new Button();
    private Double offset = 0d;

    /**
     * Temporary file path if required.
     */
    private Path tempFilePath = null;

    private MediaPlayerEventCallback medPlEvtCB = new MediaPlayerEventCallback();
    private MediaEventCallback medEvtCB = new MediaEventCallback();
//CHECKSTYLE:ON

    /**
     * Constructor.Make a new player.
     *
     * @param width Initial target width.
     * @param height Initial target height.
     * @param fact Common factory for the embedded media player.
     */
    protected Player(final double width, final double height, final MediaPlayerFactory fact) {
        targetWidth.set(width);
        targetHeight.set(height);
        mediaPlayerFactory = fact;
        embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        embeddedMediaPlayer.videoSurface().set(new VideoSurface());

        videoImageView = new ImageView();
        videoImageView.setPreserveRatio(true);
        videoImageView.fitWidthProperty().bind(targetWidth);
        videoImageView.fitHeightProperty().bind(targetHeight);

        playerVbox.maxHeightProperty().bind(targetHeight);
        playerVbox.minWidthProperty().bind(targetWidth);
        playerVbox.setMinHeight(height);
        VBox.setVgrow(playerVbox, Priority.ALWAYS);
        playerVbox.setStyle("-fx-background-color: black;");

        controlHbox.minWidthProperty().bind(targetWidth);
        controlHbox.setMaxHeight(CTRL_MAX_HEIGHT);
        controlHbox.setStyle("-fx-background-color: lightgray;");
        playerVbox.setAlignment(Pos.CENTER);
        controlHbox.setAlignment(Pos.CENTER);
        playPauseButton.setText("▶");

        playPauseButton.setOnAction(a -> {
            var ctxt = playPauseButton.getText();
            if (ctxt.equals("⏸")) {
                pause();
            } else {
                play();
            }

        });

        this.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
                if (!newValue) {
                    pause(); //Pause on hide
                }
            }
        });

        timeLabel.setText("00:00:00");
        controlSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
                var ms = newValue.doubleValue();
                if (controlSlider.isValueChanging()) {
                    embeddedMediaPlayer.controls().setTime((long) ms);
                }
                long seconds = Duration.ofMillis((long) ms).getSeconds();
                //CHECKSTYLE:OFF
                long HH = seconds / 3600;
                long MM = (seconds % 3600) / 60;
                long SS = seconds % 60;
                //CHECKSTYLE:ON
                timeLabel.setText(String.format("%02d:%02d:%02d", HH, MM, SS));
            }
        });
//CHECKSTYLE:OFF
        volSlider.setMaxWidth(75d);
//CHECKSTYLE:ON
        volSlider.setVisible(false);
        volSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
                if (volSlider.isValueChanging()) {
                    embeddedMediaPlayer.audio().setVolume(newValue.intValue());
                }
            }
        });
//Hide/show the volume slider on click
        volSlider.setManaged(volSlider.isVisible());
        volLabel.setOnMouseClicked(e -> {
            volSlider.setVisible(!volSlider.isVisible());
            volSlider.setManaged(volSlider.isVisible());
        });

        controlHbox.getChildren().addAll(playPauseButton, controlSlider, timeLabel, volLabel, volSlider);
        playerVbox.getChildren().addAll(videoImageView, controlHbox);
        this.getChildren().add(playerVbox);
    }

    /**
     * Play media from an MRL.
     *
     * @param mrl Media location.
     * @param newoffset Seek offset (in seconds).
     */
    public void load(final String mrl, final Double newoffset) {
        embeddedMediaPlayer.media().play(mrl);
        embeddedMediaPlayer.media().events().addMediaEventListener(medEvtCB);
        embeddedMediaPlayer.events().addMediaPlayerEventListener(medPlEvtCB);
        this.offset = newoffset;
        pause();
        controlSlider.setValue(newoffset);
        controlSlider.setMin(0);
        controlSlider.setMax(newoffset + 100);
    }

    /**
     * Play media "from" an input stream.Warning: I'm writing your stream to a
     * temp file as vlcj really doesn't enjoy streams. As such, this is really
     * slow.
     *
     * @param mediaBytes media in memory as bytes.
     * @param offset Offset to start at.
     * @throws java.io.IOException Couldn't open or write to temp file.
     */
    public void load(final byte[] mediaBytes, final Double offset) throws IOException {
        tempFilePath = Files.createTempFile("spres-media", null);
        Files.write(tempFilePath, mediaBytes);
        var tempMrl = tempFilePath.toAbsolutePath().toUri().toString();
        this.load(tempMrl, offset);
    }

    /**
     * Set player offset.
     *
     * @param seekOffset Offset.
     */
    public void setSeek(final Double seekOffset) {
        embeddedMediaPlayer.controls().setTime((long) (seekOffset * 1000));
    }

    /**
     * Set playback loop.
     *
     * @param loop loop playback.
     */
    public void setLoop(final Boolean loop) {
        embeddedMediaPlayer.controls().setRepeat(loop);
    }

    /**
     * Pause the media if possible.
     */
    public void pause() {
        embeddedMediaPlayer.controls().setPause(true);
        playPauseButton.setText("▶");
    }

    /**
     * Play the media if possible.
     */
    public void play() {
        embeddedMediaPlayer.controls().setPause(false);
        playPauseButton.setText("⏸");
    }

    /**
     * Set properties.
     *
     * @param props Properties.
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
     * Resize the player.
     *
     * @param size Size to target.
     */
    @Override
    public void setSize(final SizeObj size) {
        targetWidth.set(size.getX());
        targetHeight.set(size.getY());
        this.setRotate(size.getRot());
    }

    /**
     * Show the control box.
     */
    public void showControls() {
        controlHbox.setVisible(true);
        controlHbox.setManaged(true);
    }

    /**
     * Hide the control box.
     */
    public void hideControls() {
        controlHbox.setVisible(false);
        controlHbox.setManaged(false);
    }
//CHECKSTYLE:OFF
    /**
     * Private class provides a JFX compatible video surface, responding to
     * callbacks from vlcj.
     */
    private class VideoSurface extends CallbackVideoSurface {

        VideoSurface() {
            super(new FXBufferFormatCallback(), new FXRenderCallback(), true, VideoSurfaceAdapters.getVideoSurfaceAdapter());
        }
    }

    /**
     * Private class to get an RV32 buffer format object when requested.
     */
    private class FXBufferFormatCallback implements BufferFormatCallback {

        private int sourceWidth;
        private int sourceHeight;

        /**
         * Return an RV32 buffer format object for the given width/height.
         *
         * @param newSourceWidth Width of source.
         * @param newSourceHeight Height of source.
         * @return RV32 buffer.
         */
        @Override
        public BufferFormat getBufferFormat(final int newSourceWidth, final int newSourceHeight) {
            this.sourceWidth = newSourceWidth;
            this.sourceHeight = newSourceHeight;
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

        /**
         * Called when a new buffer is allocated.
         *
         * @param buffers Allocated buffers.
         */
        @Override
        public void allocatedBuffers(final ByteBuffer[] buffers) {
            //CHECKSTYLE:OFF
            assert buffers[0].capacity() == sourceWidth * sourceHeight * 4; //Buffers are 32-bit RV32 format)
            //CHECKSTYLE:ON
            PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraPreInstance();
            videoPixelBuffer = new PixelBuffer<>(sourceWidth, sourceHeight, buffers[0], pixelFormat);
            videoImage = new WritableImage(videoPixelBuffer);
            videoImageView.setImage(videoImage);
        }
    }

    /**
     * Called when rendering required.
     */
    private class FXRenderCallback implements RenderCallback {

        /**
         * Render information.
         *
         * @param mediaPlayer Rendering player.
         * @param nativeBuffers Frame data.
         * @param bufferFormat Format of frame.
         */
        @Override
        public void display(final MediaPlayer mediaPlayer, final ByteBuffer[] nativeBuffers, final BufferFormat bufferFormat) {
            Platform.runLater(() -> {
                videoPixelBuffer.updateBuffer(pb -> null); //Redraw buffer.
            });
        }
    }

    /**
     * Release native resources.
     */
    public void free() {
        embeddedMediaPlayer.events().removeMediaEventListener(medEvtCB);
        embeddedMediaPlayer.events().removeMediaPlayerEventListener(medPlEvtCB);
        embeddedMediaPlayer.release();
        try {
            Thread.sleep(1L);
        } catch (InterruptedException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (tempFilePath != null) {
            //We made a temp file, so should now remove it.
            try {
                Files.delete(tempFilePath);
            } catch (IOException ex) {
                //Yikes! We made the file, but can't delete it.
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class MediaPlayerEventCallback extends MediaPlayerEventAdapter {

        /**
         * Time in Media has changed. Update slider.
         *
         * @param mediaPlayer Player.
         * @param newTime Time at.
         */
        @Override
        public void timeChanged(final MediaPlayer mediaPlayer, final long newTime) {
            Platform.runLater(() -> {
                //CHECKSTYLE:OFF
                if (!controlSlider.isValueChanging()) {
                    controlSlider.setValue((double) newTime);
                }
                //CHECKSTYLE:ON
            });
        }

        /**
         * Media has paused.
         *
         * @param mediaPlayer player.
         */
        @Override
        public void paused(final MediaPlayer mediaPlayer) {
        }

        /**
         * Media is playing.
         *
         * @param mediaPlayer player.
         */
        @Override
        public void playing(final MediaPlayer mediaPlayer) {
        }

    }

    /**
     * Called when media changes.
     */
    private class MediaEventCallback extends MediaEventAdapter {

        /**
         * Duration of media changed.
         *
         * @param media Media.
         * @param l Duration (mS).
         */
        @Override
        public void mediaDurationChanged(final Media media, final long l) {
            controlSlider.setMax((double) l);

            embeddedMediaPlayer.controls().setTime((long) (offset * 1000));
        }

        /**
         * Some state has changed.
         *
         * @param media Media.
         * @param state State update.
         */
        @Override
        public void mediaStateChanged(final Media media, final State state) {
            if (state == State.PLAYING || state == State.PAUSED) {
                //We should now be able to set the volume slider correctly.
                volSlider.setValue(embeddedMediaPlayer.audio().volume());
            }
        }
    }
    //CHECKSTYLE:ON
}
