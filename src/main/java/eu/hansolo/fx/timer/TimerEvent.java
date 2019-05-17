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

public class TimerEvent {
    public enum Type {
        STARTED, STOPPED, CONTINUED, FINISHED, RESET, WAITING, SECOND
    }

    private final Timer timer;
    private final Type  type;


    // ******************** Constructor ***************************************
    public TimerEvent(final Timer TIMER, final Type TYPE) {
        timer = TIMER;
        type  = TYPE;
    }


    // ******************** Methods *******************************************
    public Timer getTimer() { return timer; }

    public Type getType() { return type; }
}
