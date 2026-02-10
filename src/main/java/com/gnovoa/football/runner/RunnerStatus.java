package com.gnovoa.football.runner;

import com.gnovoa.football.model.League;

import java.time.Instant;

public record RunnerStatus(
        League league,
        RunnerState state,
        String seasonId,
        int seasonIndex,
        int roundIndex,
        String runningFixtureId,
        Integer runningRound,
        Instant nextStartAt,
        String nextAction
) {}
