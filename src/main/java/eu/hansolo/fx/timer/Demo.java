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

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.util.Locale;


/**
 * User: hansolo
 * Date: 11.10.17
 * Time: 17:57
 */
public class Demo extends Application {
    private Timer timer;
    private Label label;


    @Override public void init() {
        timer = TimerBuilder.create()
                            .playButtonVisible(false)
                            .waitingColor(Color.GRAY)
                            .duration(Duration.seconds(30))
                            .prefSize(38, 38)
                            .build();
        timer.setOnTimerEvent(event -> {
            switch(event.getType()) {
                case STARTED  : break;
                case CONTINUED: break;
                case STOPPED  : timer.setPlayButtonVisible(true);break;
                case FINISHED : label.setText("0 s"); break;
                case WAITING  : break;
                case SECOND   : label.setText(String.format(Locale.US, "%.1f s", timer.getCurrentTime().toSeconds()));
            }
            System.out.println(event.getType());
        });
        label = new Label();
    }

    @Override public void start(Stage stage) {
        VBox pane = new VBox(10, timer, label);
        pane.setPadding(new Insets(30));

        Scene scene = new Scene(pane);

        stage.setTitle("Timer");
        stage.setScene(scene);
        stage.show();

        timer.waiting();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
