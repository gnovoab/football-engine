package com.gnovoa.football.runner;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class RunnerBootstrap {

    private final RunnerProperties props;
    private final List<LeagueSeasonRunner> runners;

    public RunnerBootstrap(RunnerProperties props, List<LeagueSeasonRunner> runners) {
        this.props = props;
        this.runners = runners;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (!props.autoStartOnBoot()) return;
        runners.forEach(LeagueSeasonRunner::startOrResume);
    }
}