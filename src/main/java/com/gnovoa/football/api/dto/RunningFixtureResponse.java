package com.gnovoa.football.api.dto;

import com.gnovoa.football.model.League;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record RunningFixtureResponse(
        League league,
        String status,
        String seasonId,
        int seasonIndex,
        int round,
        String fixtureId,
        Map<String, String> ws,
        List<MatchItem> matches,
        NextAction next
) {
    public record MatchItem(String matchId, String homeTeam, String awayTeam, Map<String, String> ws) {}
    public record NextAction(String action, Instant startsAt, long startsInSeconds) {}
}