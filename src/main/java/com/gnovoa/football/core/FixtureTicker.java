package com.gnovoa.football.core;
import com.gnovoa.football.rosters.SimProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

public final class FixtureTicker {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<MatchEngine> engines;
    private final int tickMillis;

    private Instant lastTick;

    public FixtureTicker(List<MatchEngine> engines, SimProperties props) {
        this.engines = engines;
        this.tickMillis = props.tickMillis();
    }

    public void start(Runnable onAllFinished) {
        lastTick = Instant.now();
        scheduler.scheduleAtFixedRate(() -> tick(onAllFinished), 0, tickMillis, TimeUnit.MILLISECONDS);
    }

    public void stopNow() {
        scheduler.shutdownNow();
    }

    private void tick(Runnable onAllFinished) {
        Instant now = Instant.now();
        Duration dt = Duration.between(lastTick, now);
        lastTick = now;

        for (MatchEngine e : engines) e.advance(dt);

        boolean allFinished = engines.stream().allMatch(MatchEngine::isFinished);
        if (allFinished) {
            onAllFinished.run();
            stopNow();
        }
    }
}