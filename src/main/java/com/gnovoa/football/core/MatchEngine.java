package com.gnovoa.football.core;

import com.gnovoa.football.events.MatchEvent;
import com.gnovoa.football.out.EventPublisher;
import com.gnovoa.football.sim.SituationPlanner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class MatchEngine {

    private final MatchState state;
    private final EventPublisher publisher;
    private final SituationPlanner planner;

    private Duration acc = Duration.ZERO;
    private boolean kickedOff = false;

    public MatchEngine(MatchState state, EventPublisher publisher, SituationPlanner planner) {
        this.state = state;
        this.publisher = publisher;
        this.planner = planner;
    }

    public MatchState state() { return state; }
    public boolean isFinished() { return state.isFinished(); }

    public void kickOffIfNeeded() {
        if (kickedOff) return;
        kickedOff = true;
        publisher.publish(state.eventNow(com.gnovoa.football.events.FootballEventType.KICK_OFF, java.util.Map.of()));
    }

    public void advance(Duration dt) {
        if (state.isFinished()) return;

        acc = acc.plus(dt);
        while (acc.compareTo(Duration.ofSeconds(1)) >= 0) {
            acc = acc.minus(Duration.ofSeconds(1));

            kickOffIfNeeded();

            // clock/phase/snapshot events
            state.advanceOneSecond().ifPresent(publisher::publish);

            // rich-ish incidents during play
            List<MatchEvent> events = new ArrayList<>(planner.maybeGenerate(state));
            events.forEach(publisher::publish);
        }
    }
}
