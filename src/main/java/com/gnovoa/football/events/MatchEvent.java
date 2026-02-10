package com.gnovoa.football.events;

import com.gnovoa.football.model.League;

import java.time.Instant;
import java.util.Map;

public record MatchEvent(
        League league,
        String seasonId,
        String fixtureId,
        String matchId,
        Instant occurredAt,
        MatchClockSnapshot match,
        FootballEventType type,
        Map<String, Object> data
) {}
