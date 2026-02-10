package com.gnovoa.football.model;

import java.util.List;

public record Team(
    String teamId, String name, String shortName, TeamStrength strength, List<Player> players) {}
