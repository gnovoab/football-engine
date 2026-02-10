package com.gnovoa.football.model;

import java.util.List;

public record LeagueRoster(League league, String season, List<Team> teams) {}
