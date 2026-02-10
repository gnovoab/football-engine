package com.gnovoa.football.runner;

import com.gnovoa.football.core.FixtureRuntime;
import com.gnovoa.football.model.League;

public interface LeagueSeasonRunner {
    League league();
    RunnerStatus status();

    void startOrResume();
    void pause();
    void stop();

    SeasonPlan currentSeasonPlan();
    FixtureRuntime runningFixture();
}