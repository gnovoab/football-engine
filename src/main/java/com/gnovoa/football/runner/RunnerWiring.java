package com.gnovoa.football.runner;

import com.gnovoa.football.core.FixtureRuntimeFactory;
import com.gnovoa.football.model.League;
import com.gnovoa.football.rosters.RosterCatalog;
import com.gnovoa.football.schedule.RoundRobinScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RunnerWiring {

    @Bean
    public RoundRobinScheduler roundRobinScheduler() {
        return new RoundRobinScheduler();
    }

    @Bean
    public FixtureRuntimeFactory fixtureRuntimeFactory(com.gnovoa.football.out.EventPublisher publisher, com.gnovoa.football.rosters.SimProperties simProps) {
        return new FixtureRuntimeFactory(publisher, simProps);
    }

    @Bean
    public LeagueSeasonRunner premierLeagueRunner(RosterCatalog rosters, RoundRobinScheduler scheduler, FixtureRuntimeFactory factory, RunnerProperties props) {
        return new LeagueRunner(League.PREMIER_LEAGUE, rosters, scheduler, factory, props);
    }

    @Bean
    public LeagueSeasonRunner serieARunner(RosterCatalog rosters, RoundRobinScheduler scheduler, FixtureRuntimeFactory factory, RunnerProperties props) {
        return new LeagueRunner(League.SERIE_A, rosters, scheduler, factory, props);
    }

    @Bean
    public LeagueSeasonRunner laLigaRunner(RosterCatalog rosters, RoundRobinScheduler scheduler, FixtureRuntimeFactory factory, RunnerProperties props) {
        return new LeagueRunner(League.LA_LIGA, rosters, scheduler, factory, props);
    }
}