package com.gnovoa.football.events;

public record MatchClockSnapshot(
        String phase,
        int second,
        int minute,
        int stoppageSecond,
        int homeScore,
        int awayScore
) {}
