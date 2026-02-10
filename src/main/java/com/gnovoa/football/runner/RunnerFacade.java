package com.gnovoa.football.runner;

import com.gnovoa.football.api.dto.LeagueScheduleResponse;
import com.gnovoa.football.api.dto.RunningFixtureResponse;
import com.gnovoa.football.core.FixtureRuntime;
import com.gnovoa.football.model.League;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public final class RunnerFacade {

    private final RunnerRegistry registry;

    public RunnerFacade(RunnerRegistry registry) {
        this.registry = registry;
    }

    public RunningFixtureResponse getRunningFixtureOrNext(League league) {
        LeagueSeasonRunner runner = registry.runner(league);
        RunnerStatus st = runner.status();
        FixtureRuntime fx = runner.runningFixture();

        if (fx != null) {
            String fixtureWs = "/ws/leagues/" + league.name() + "/fixtures/" + fx.fixtureId();
            List<RunningFixtureResponse.MatchItem> matches = fx.matches().stream()
                    .map(m -> new RunningFixtureResponse.MatchItem(
                            m.matchId(),
                            m.homeTeam(),
                            m.awayTeam(),
                            Map.of("match", "/ws/matches/" + m.matchId())
                    ))
                    .toList();

            return new RunningFixtureResponse(
                    league,
                    "RUNNING",
                    fx.seasonId(),
                    fx.seasonIndex(),
                    fx.round(),
                    fx.fixtureId(),
                    Map.of("fixture", fixtureWs),
                    matches,
                    null
            );
        }

        // Not running
        boolean showNext = (st.state() == RunnerState.WAITING_NEXT_FIXTURE || st.state() == RunnerState.WAITING_NEXT_SEASON);
        RunningFixtureResponse.NextAction next = null;

        if (showNext && st.nextStartAt() != null) {
            long sec = Math.max(0, Duration.between(Instant.now(), st.nextStartAt()).getSeconds());
            next = new RunningFixtureResponse.NextAction(st.nextAction(), st.nextStartAt(), sec);
        }

        // round semantics: return the NEXT round to be started (simplest for UI)
        int round = st.roundIndex();

        return new RunningFixtureResponse(
                league,
                st.state().name(),
                st.seasonId(),
                st.seasonIndex(),
                round,
                null,
                null,
                List.of(),
                next
        );
    }

    public LeagueScheduleResponse getSchedule(League league) {
        LeagueSeasonRunner runner = registry.runner(league);
        RunnerStatus st = runner.status();
        SeasonPlan plan = runner.currentSeasonPlan();

        FixtureRuntime fx = runner.runningFixture();
        Integer currentRound = (fx != null) ? fx.round() : null;

        Integer nextRound = null;
        if (st.state() != RunnerState.DONE) {
            nextRound = st.roundIndex(); // next to start (or current pointer)
            if (fx != null && fx.round() < 38) nextRound = fx.round() + 1;
            if (st.state() == RunnerState.WAITING_NEXT_SEASON) nextRound = 1;
        }

        var rounds = plan.fixtures38().stream()
                .map(f -> new LeagueScheduleResponse.RoundItem(
                        f.roundIndex(),
                        f.matches().stream()
                                .map(p -> new LeagueScheduleResponse.Matchup(p.home().name(), p.away().name()))
                                .toList()
                ))
                .toList();

        return new LeagueScheduleResponse(
                league,
                plan.seasonId(),
                plan.seasonIndex(),
                currentRound,
                nextRound,
                rounds
        );
    }
}