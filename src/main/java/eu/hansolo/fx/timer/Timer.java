/*
 * Copyright (c) 2017 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.timer;

import eu.hansolo.fx.timer.TimerEvent.Type;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * User: hansolo
 * Date: 11.10.17
 * Time: 17:48
 */
@DefaultProperty("children")
public class Timer extends Region {
    public  enum         State { RUNNING, WAITING, STOPPED }
    public  static final Color                    DEFAULT_COLOR    = Color.web("0x407DBD");
    private static final double                   PREFERRED_WIDTH  = 19;
    private static final double                   PREFERRED_HEIGHT = 19;
    private static final double                   MINIMUM_WIDTH    = 19;
    private static final double                   MINIMUM_HEIGHT   = 19;
    private static final double                   MAXIMUM_WIDTH    = 1024;
    private static final double                   MAXIMUM_HEIGHT   = 1024;
    private        final TimerEvent               STARTED          = new TimerEvent(Timer.this, Type.STARTED);
    private        final TimerEvent               STOPPED          = new TimerEvent(Timer.this, Type.STOPPED);
    private        final TimerEvent               CONTINUED        = new TimerEvent(Timer.this, Type.CONTINUED);
    private        final TimerEvent               FINISHED         = new TimerEvent(Timer.this, Type.FINISHED);
    private        final TimerEvent               RESET            = new TimerEvent(Timer.this, Type.RESET);
    private        final TimerEvent               WAITING          = new TimerEvent(Timer.this, Type.WAITING);
    private        final TimerEvent               SECOND           = new TimerEvent(Timer.this, Type.SECOND);
    private              double                   size;
    private              double                   width;
    private              double                   height;
    private              double                   centerX;
    private              double                   centerY;
    private              Pane                     pane;
    private              Paint                    backgroundPaint;
    private              Paint                    borderPaint;
    private              double                   borderWidth;
    private              Arc                      ring;
    private              Arc                      progressBar;
    private              Rectangle                stopButton;
    private              Path                     playButton;
    private              MoveTo                   playButtonP1;
    private              LineTo                   playButtonP2;
    private              LineTo                   playButtonP3;
    private              Color                    _backgroundColor;
    private              ObjectProperty<Color>    backgroundColor;
    private              Color                    _color;
    private              ObjectProperty<Color>    color;
    private              Color                    _waitingColor;
    private              ObjectProperty<Color>    waitingColor;
    private              boolean                  _playButtonVisible;
    private              BooleanProperty          playButtonVisible;
    private              DoubleProperty           progress;
    private              State                    state;
    private              Duration                 _duration;
    private              ObjectProperty<Duration> duration;
    private              Duration                 currentDuration;
    private              Timeline                 timeline;
    private              List<TimerEventListener> listenerList = new CopyOnWriteArrayList<>();


    // ******************** Constructors **************************************
    public Timer() {
        getStylesheets().add(Timer.class.getResource("timer.css").toExternalForm());
        backgroundPaint    = Color.TRANSPARENT;
        borderPaint        = Color.TRANSPARENT;
        borderWidth        = 0d;
        _backgroundColor   = Color.TRANSPARENT;
        _color             = DEFAULT_COLOR;
        _waitingColor      = DEFAULT_COLOR;
        _playButtonVisible = true;
        state              = State.STOPPED;
        _duration          = Duration.seconds(10);
        currentDuration    = Duration.ZERO;
        progress           = new DoublePropertyBase(0) {
            @Override protected void invalidated() { progressBar.setLength(-360.0 * get()); }
            @Override public Object getBean() { return Timer.this; }
            @Override public String getName() { return "progress"; }
        };
        timeline           = new Timeline();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
            Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        getStyleClass().add("timer");

        ring = new Arc();
        ring.setStartAngle(0);
        ring.setLength(360);
        ring.setStroke(_color);
        ring.setStrokeType(StrokeType.INSIDE);

        progressBar = new Arc();
        progressBar.setType(ArcType.OPEN);
        progressBar.setFill(_backgroundColor);
        progressBar.setStroke(_color);
        progressBar.setStrokeLineCap(StrokeLineCap.BUTT);
        progressBar.setStartAngle(90);
        progressBar.setLength(0);
        progressBar.setMouseTransparent(true);

        stopButton = new Rectangle();
        stopButton.setVisible(false);
        stopButton.setManaged(false);
        stopButton.setStroke(null);
        stopButton.setMouseTransparent(true);

        playButtonP1 = new MoveTo();
        playButtonP2 = new LineTo();
        playButtonP3 = new LineTo();
        playButton = new Path(playButtonP1, playButtonP2, playButtonP3, new ClosePath());
        playButton.setStroke(null);
        playButton.setMouseTransparent(true);

        pane = new Pane(ring, progressBar, stopButton, playButton);
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(borderWidth))));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        timeline.setOnFinished(event -> {
            finished();
            fireTimerEvent(FINISHED);
        });
        timeline.currentTimeProperty().addListener((o, ov, nv) -> {
            if ((int) nv.toSeconds() > (int) ov.toSeconds()) { fireTimerEvent(SECOND); }
        });
        ring.setOnMousePressed(event -> {
            switch(state) {
                case RUNNING: stop();break;
                case STOPPED: if (currentDuration.greaterThan(Duration.ZERO)) { startFromCurrent(); } else { start(); } break;
                case WAITING: stop();break;
            }
        });
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public Color getBackgroundColor() { return null == backgroundColor ? _backgroundColor : backgroundColor.get(); }
    public void setBackgroundColor(final Color COLOR) {
        if (null == backgroundColor) {
            _backgroundColor = COLOR;
            redraw();
        } else {
            backgroundColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> backgroundColorProperty() {
        if (null == backgroundColor) {
            backgroundColor = new ObjectPropertyBase<Color>(_backgroundColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return Timer.this; }
                @Override public String getName() { return "backgroundColor"; }
            };
            _backgroundColor = null;
        }
        return backgroundColor;
    }

    public Color getColor() { return null == color ? _color : color.get(); }
    public void setColor(final Color COLOR) {
        if (null == color) {
            _color = COLOR;
            redraw();
        } else {
            color.set(COLOR);
        }
    }
    public ObjectProperty<Color> colorProperty() {
        if (null == color) {
            color = new ObjectPropertyBase<Color>(_color) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return Timer.this; }
                @Override public String getName() { return "color"; }
            };
            _color = null;
        }
        return color;
    }

    public Color getWaitingColor() { return null == waitingColor ? _waitingColor : waitingColor.get(); }
    public void setWaitingColor(final Color COLOR) {
        if (null == waitingColor) {
            _waitingColor = COLOR;
            redraw();
        } else {
            waitingColor.set(COLOR);
            redraw();
        }
    }
    public ObjectProperty<Color> waitingColorProperty() {
        if (null == waitingColor) {
            waitingColor = new ObjectPropertyBase<Color>(_waitingColor) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return Timer.this; }
                @Override public String getName() { return "waitingColor"; }
            };
            _waitingColor = null;
        }
        return waitingColor;
    }

    public boolean isPlayButtonVisible() { return null == playButtonVisible ? _playButtonVisible : playButtonVisible.get(); }
    public void setPlayButtonVisible(final boolean VISIBLE) {
        if (null == playButtonVisible) {
            _playButtonVisible = VISIBLE;
            if (VISIBLE) { enableNode(playButton); } else { disableNode(playButton); }
        } else {
            playButtonVisible.set(VISIBLE);
        }
    }
    public BooleanProperty playButtonVisibleProperty() {
        if (null == playButtonVisible) {
            playButtonVisible = new BooleanPropertyBase(_playButtonVisible) {
                @Override protected void invalidated() { if (get()) { enableNode(playButton); } else { disableNode(playButton); }}
                @Override public Object getBean() { return Timer.this; }
                @Override public String getName() { return "playButtonVisible"; }
            };
        }
        return playButtonVisible;
    }

    public double getProgress() { return progress.get(); }
    public void setProgress(final double PROGRESS) { progress.set(clamp(0.0, 1.0, PROGRESS)); }
    public ReadOnlyDoubleProperty progressProperty() { return progress; }

    public Duration getDuration() { return null == duration ? _duration : duration.get(); }
    public void setDuration(final Duration DURATION) {
        if (null == duration) {
            _duration = DURATION;
        } else {
            duration.set(DURATION);
        }
    }
    public ObjectProperty<Duration> durationProperty() {
        if (null == duration) {
            duration = new ObjectPropertyBase<Duration>(_duration) {
                @Override public Object getBean() { return Timer.this; }
                @Override public String getName() { return "duration"; }
            };
            _duration = null;
        }
        return duration;
    }

    public Duration getCurrentTime() { return timeline.getCurrentTime(); }

    public ReadOnlyObjectProperty<Duration> currentTimeProperty() { return timeline.currentTimeProperty(); }

    public void start() {
        ring.setLength(360);

        KeyValue kv0 = new KeyValue(progress, 0.0);
        KeyValue kv1 = new KeyValue(progress, 1.0);

        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv0);
        KeyFrame kf1 = new KeyFrame(getDuration(), kv1);

        timeline.getKeyFrames().setAll(kf0, kf1);

        if (isPlayButtonVisible()) { disableNode(playButton); }
        enableNode(stopButton);

        timeline.setCycleCount(1);
        timeline.playFromStart();

        state = State.RUNNING;
        fireTimerEvent(STARTED);
    }
    public void startFromCurrent() {
        ring.setLength(360);

        KeyValue kv0 = new KeyValue(progress, 0.0);
        KeyValue kv1 = new KeyValue(progress, 1.0);

        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv0);
        KeyFrame kf1 = new KeyFrame(getDuration(), kv1);

        timeline.getKeyFrames().setAll(kf0, kf1);
        timeline.jumpTo(currentDuration);

        if (isPlayButtonVisible()) { disableNode(playButton); }
        enableNode(stopButton);

        timeline.setCycleCount(1);
        timeline.play();

        state = State.RUNNING;
        fireTimerEvent(CONTINUED);
    }
    public void stop() {
        currentDuration = state == State.WAITING ? Duration.ZERO : timeline.getCurrentTime();
        timeline.stop();
        timeline.setCycleCount(1);

        ring.setLength(360);
        ring.setStroke(getColor());

        disableNode(stopButton);
        stopButton.setFill(getColor());
        if (isPlayButtonVisible()) { enableNode(playButton); }

        state = State.STOPPED;
        fireTimerEvent(STOPPED);
    }
    public void reset() {
        finished();
        fireTimerEvent(RESET);
    }
    public void waiting() {
        timeline.stop();
        KeyValue kv0 = new KeyValue(ring.rotateProperty(), 0);
        KeyValue kv1 = new KeyValue(ring.rotateProperty(), 360);

        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv0);
        KeyFrame kf1 = new KeyFrame(Duration.seconds(1), kv1);

        timeline.getKeyFrames().setAll(kf0, kf1);

        ring.setLength(300);
        ring.setStroke(getWaitingColor());

        if (isPlayButtonVisible()) { disableNode(playButton); }
        stopButton.setFill(getWaitingColor());
        enableNode(stopButton);

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        state = State.WAITING;
        fireTimerEvent(WAITING);
    }
    private void finished() {
        timeline.stop();
        timeline.setCycleCount(1);

        ring.setLength(360);
        setProgress(0);
        currentDuration = Duration.ZERO;

        disableNode(stopButton);
        if (isPlayButtonVisible()) { enableNode(playButton); }

        state = State.STOPPED;
    }

    private double clamp(final double min, final double max, final double value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private void enableNode(final Node NODE) {
        NODE.setManaged(true);
        NODE.setVisible(true);
    }
    private void disableNode(final Node NODE) {
        NODE.setVisible(false);
        NODE.setManaged(false);
    }


    // ******************** EventHandling *************************************
    public void setOnTimerEvent(final TimerEventListener LISTENER) { addTimerEventListener(LISTENER); }
    public void addTimerEventListener(final TimerEventListener LISTENER) { if (!listenerList.contains(LISTENER)) listenerList.add(LISTENER); }
    public void removeTimerEventListener(final TimerEventListener LISTENER) { if (listenerList.contains(LISTENER)) listenerList.remove(LISTENER); }

    public void fireTimerEvent(final TimerEvent EVENT) {
        for (TimerEventListener listener : listenerList) { listener.onTimerEvent(EVENT); }
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width   = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height  = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size    = width < height ? width : height;
        centerX = size * 0.5;
        centerY = size * 0.5;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            ring.setCenterX(centerX);
            ring.setCenterY(centerY);
            ring.setRadiusX(centerX);
            ring.setRadiusY(centerY);
            ring.setStrokeWidth(size * 0.05263158);

            progressBar.setCenterX(centerX);
            progressBar.setCenterY(centerY);
            progressBar.setRadiusX(size * 0.44736842);
            progressBar.setRadiusY(size * 0.44736842);
            progressBar.setStrokeWidth(size * 0.10526316);

            stopButton.setWidth(size * 0.26315789);
            stopButton.setHeight(size * 0.26315789);
            stopButton.relocate((centerX - size * 0.13157895), (centerY - size * 0.13157895));

            playButtonP1.setX(size * 0.36842105);
            playButtonP1.setY(size * 0.26315789);
            playButtonP2.setX(size * 0.73684211);
            playButtonP2.setY(size * 0.5);
            playButtonP3.setX(size * 0.36842105);
            playButtonP3.setY(size * 0.73684211);

            redraw();
        }
    }

    private void redraw() {
        ring.setFill(getBackgroundColor());
        ring.setStroke(state == State.WAITING ? getWaitingColor() : getColor());
        progressBar.setFill(getBackgroundColor());
        progressBar.setStroke(getColor());
        stopButton.setFill(state == State.WAITING ? getWaitingColor() : getColor());
        playButton.setFill(getColor());
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(borderWidth / PREFERRED_WIDTH * size))));
    }
}
