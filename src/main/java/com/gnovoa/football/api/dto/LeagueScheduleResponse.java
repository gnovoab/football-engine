package com.gnovoa.football.api.dto;

import com.gnovoa.football.model.League;

import java.util.List;

public record LeagueScheduleResponse(
        League league,
        String seasonId,
        int seasonIndex,
        Integer currentRound,
        Integer nextRound,
        List<RoundItem> rounds
) {
    public record RoundItem(int round, List<Matchup> matches) {}
    public record Matchup(String homeTeam, String awayTeam) {}
}