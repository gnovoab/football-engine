package com.gnovoa.football.runner;


import com.gnovoa.football.model.League;
import com.gnovoa.football.schedule.RoundRobinScheduler;

import java.util.List;

public record SeasonPlan(
        League league,
        String seasonId,
        int seasonIndex,
        List<RoundRobinScheduler.Fixture> fixtures38
) {}