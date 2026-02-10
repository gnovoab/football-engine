package com.gnovoa.football.model;

public record Player(
    String playerId,
    String name,
    Position position,
    int shirt,
    int rating,
    int discipline,
    int stamina) {}
