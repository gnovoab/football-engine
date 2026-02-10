package com.gnovoa.football.runner;

import com.gnovoa.football.core.FixtureRuntime;
import com.gnovoa.football.model.League;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public final class RunnerRegistry {

    private final Map<League, LeagueSeasonRunner> runners = new EnumMap<>(League.class);

    public RunnerRegistry(List<LeagueSeasonRunner> runnersList) {
        for (var r : runnersList) runners.put(r.league(), r);
    }

    public LeagueSeasonRunner runner(League league) {
        LeagueSeasonRunner r = runners.get(league);
        if (r == null) throw new IllegalArgumentException("Unknown league " + league);
        return r;
    }

    public FixtureRuntime runningFixture(League league) {
        return runner(league).runningFixture();
    }
}