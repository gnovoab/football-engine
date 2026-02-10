package com.gnovoa.football.runner;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "runner")
public record RunnerProperties(
        int seasonsToRun,
        int gapBetweenFixturesMinutes,
        int gapBetweenSeasonsMinutes,
        boolean autoStartOnBoot
) {}