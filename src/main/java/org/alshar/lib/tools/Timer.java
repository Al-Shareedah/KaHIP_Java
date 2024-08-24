package org.alshar.lib.tools;

import java.time.Duration;
import java.time.Instant;

public class Timer {

    private Instant start;

    public Timer() {
        this.start = Instant.now();
    }

    public void restart() {
        this.start = Instant.now();
    }

    public double elapsed() {
        Duration duration = Duration.between(start, Instant.now());
        return duration.toMillis() / 1000.0;
    }
}

