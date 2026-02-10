package com.gnovoa.football.core;

import com.gnovoa.football.model.League;
import com.gnovoa.football.model.Team;
import com.gnovoa.football.out.EventPublisher;
import com.gnovoa.football.rosters.SimProperties;
import com.gnovoa.football.sim.LocalRandomSource;
import com.gnovoa.football.sim.SituationPlanner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FixtureRuntimeFactory {

    private final EventPublisher publisher;
    private final SimProperties simProps;

    public FixtureRuntimeFactory(EventPublisher publisher, SimProperties simProps) {
        this.publisher = publisher;
        this.simProps = simProps;
    }

    public FixtureRuntime create(
            League league,
            String seasonId,
            int seasonIndex,
            int round,
            com.gnovoa.football.schedule.RoundRobinScheduler.Fixture fixture
    ) {
        String fixtureId = "fx-" + UUID.randomUUID();

        var rnd = new LocalRandomSource();
        var planner = new SituationPlanner(rnd);

        List<MatchEngine> engines = new ArrayList<>();
        List<FixtureRuntime.RunningMatch> matches = new ArrayList<>();

        for (var p : fixture.matches()) {
            Team home = p.home();
            Team away = p.away();

            String matchId = "m-" + UUID.randomUUID();

            MatchState state = new MatchState(league, seasonId, fixtureId, matchId, home, away, simProps);
            MatchEngine engine = new MatchEngine(state, publisher, planner);

            engines.add(engine);
            matches.add(new FixtureRuntime.RunningMatch(matchId, home.name(), away.name()));
        }

        return new FixtureRuntimeImpl(league, seasonId, seasonIndex, round, fixtureId, engines, matches, simProps);
    }
}