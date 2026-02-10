package com.gnovoa.football.core;

import com.gnovoa.football.model.League;
import com.gnovoa.football.rosters.SimProperties;

import java.util.List;

public final class FixtureRuntimeImpl implements FixtureRuntime {

    private final League league;
    private final String seasonId;
    private final int seasonIndex;
    private final int round;
    private final String fixtureId;

    private final List<MatchEngine> engines;
    private final List<RunningMatch> matches;
    private final FixtureTicker ticker;

    private volatile boolean finished = false;
    private volatile Runnable onFinished = () -> {};

    public FixtureRuntimeImpl(
            League league,
            String seasonId,
            int seasonIndex,
            int round,
            String fixtureId,
            List<MatchEngine> engines,
            List<RunningMatch> matches,
            SimProperties props
    ) {
        this.league = league;
        this.seasonId = seasonId;
        this.seasonIndex = seasonIndex;
        this.round = round;
        this.fixtureId = fixtureId;
        this.engines = engines;
        this.matches = matches;
        this.ticker = new FixtureTicker(engines, props);
    }

    @Override public League league() { return league; }
    @Override public String seasonId() { return seasonId; }
    @Override public int seasonIndex() { return seasonIndex; }
    @Override public int round() { return round; }
    @Override public String fixtureId() { return fixtureId; }
    @Override public List<RunningMatch> matches() { return matches; }

    @Override public boolean isFinished() { return finished; }

    @Override
    public void start() {
        ticker.start(() -> {
            finished = true;
            onFinished.run();
        });
    }

    @Override
    public void stop() {
        ticker.stopNow();
        finished = true;
        onFinished.run();
    }

    @Override
    public void onFinished(Runnable callback) {
        this.onFinished = callback == null ? () -> {} : callback;
    }
}