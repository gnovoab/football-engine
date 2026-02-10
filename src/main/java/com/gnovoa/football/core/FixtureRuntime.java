package com.gnovoa.football.core;

import com.gnovoa.football.model.League;

import java.util.List;

public interface FixtureRuntime {
    League league();
    String seasonId();
    int seasonIndex();
    int round();
    String fixtureId();

    List<RunningMatch> matches(); // for API mapping

    boolean isFinished();
    void start();
    void stop();
    void onFinished(Runnable callback);

    record RunningMatch(String matchId, String homeTeam, String awayTeam) {}
}
