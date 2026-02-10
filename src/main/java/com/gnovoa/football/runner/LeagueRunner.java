package com.gnovoa.football.runner;

import com.gnovoa.football.core.FixtureRuntime;
import com.gnovoa.football.core.FixtureRuntimeFactory;
import com.gnovoa.football.model.League;
import com.gnovoa.football.rosters.RosterCatalog;
import com.gnovoa.football.schedule.RoundRobinScheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

public final class LeagueRunner implements LeagueSeasonRunner {

    private final League league;
    private final RosterCatalog rosters;
    private final RoundRobinScheduler scheduler;
    private final FixtureRuntimeFactory fixtureFactory;
    private final RunnerProperties props;

    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private volatile ScheduledFuture<?> nextTask;

    private volatile RunnerState state = RunnerState.IDLE;
    private volatile boolean pauseRequested = false;

    private SeasonPlan currentPlan;

    private int seasonIndex = 1;
    private int roundIndex = 1;

    private volatile FixtureRuntime running;

    private volatile Instant nextStartAt;
    private volatile String nextAction;

    public LeagueRunner(League league, RosterCatalog rosters, RoundRobinScheduler scheduler, FixtureRuntimeFactory fixtureFactory, RunnerProperties props) {
        this.league = league;
        this.rosters = rosters;
        this.scheduler = scheduler;
        this.fixtureFactory = fixtureFactory;
        this.props = props;
    }

    @Override public League league() { return league; }

    @Override
    public synchronized void startOrResume() {
        pauseRequested = false;

        if (state == RunnerState.DONE) return;

        if (running != null && !running.isFinished()) return;

        if (currentPlan == null || seasonIndex != currentPlan.seasonIndex()) {
            generateSeasonPlan();
        }

        // if we had a scheduled start time in the future, schedule it; if past/due, start now
        if (nextStartAt != null && Instant.now().isBefore(nextStartAt)) {
            scheduleAt(nextStartAt);
            return;
        }

        startRoundNow(roundIndex);
    }

    @Override
    public synchronized void pause() {
        pauseRequested = true;

        // If waiting, cancel timer and go PAUSED immediately
        if (state == RunnerState.WAITING_NEXT_FIXTURE || state == RunnerState.WAITING_NEXT_SEASON) {
            cancelNextTask();
            state = RunnerState.PAUSED;
        }
        // If running, we’ll switch to PAUSED when fixture completes
    }

    @Override
    public synchronized void stop() {
        pauseRequested = true;
        cancelNextTask();

        if (running != null && !running.isFinished()) {
            running.stop(); // immediate
        }
        running = null;

        state = RunnerState.STOPPED;
        // keep plan/indices so resume continues from correct point
    }

    @Override
    public synchronized RunnerStatus status() {
        Integer runningRound = (running != null && !running.isFinished()) ? running.round() : null;
        String runningFixtureId = (running != null && !running.isFinished()) ? running.fixtureId() : null;

        return new RunnerStatus(
                league,
                state,
                currentPlan == null ? null : currentPlan.seasonId(),
                seasonIndex,
                roundIndex,
                runningFixtureId,
                runningRound,
                nextStartAt,
                nextAction
        );
    }

    @Override
    public synchronized SeasonPlan currentSeasonPlan() {
        if (currentPlan == null) generateSeasonPlan();
        return currentPlan;
    }

    @Override
    public synchronized FixtureRuntime runningFixture() {
        if (running != null && !running.isFinished()) return running;
        return null;
    }

    private void generateSeasonPlan() {
        var roster = rosters.roster(league);
        var fixtures = scheduler.doubleRoundRobinReshuffledSecondLeg(roster.teams());
        currentPlan = new SeasonPlan(league, "season-" + seasonIndex, seasonIndex, fixtures);
    }

    private void startRoundNow(int round) {
        var fixture = currentPlan.fixtures38().get(round - 1);

        running = fixtureFactory.create(league, currentPlan.seasonId(), seasonIndex, round, fixture);
        state = RunnerState.RUNNING_FIXTURE;

        // clear waiting info while running
        nextStartAt = null;
        nextAction = null;

        running.onFinished(() -> onFixtureFinished(round));
        running.start();
    }

    private void onFixtureFinished(int finishedRound) {
        synchronized (this) {
            running = null;

            if (pauseRequested) {
                state = RunnerState.PAUSED;
                return;
            }

            if (finishedRound < 38) {
                roundIndex = finishedRound + 1;
                state = RunnerState.WAITING_NEXT_FIXTURE;

                nextAction = "START_NEXT_FIXTURE";
                nextStartAt = Instant.now().plus(props.gapBetweenFixturesMinutes(), ChronoUnit.MINUTES);

                scheduleAt(nextStartAt);
                return;
            }

            // season ended
            if (seasonIndex >= props.seasonsToRun()) {
                state = RunnerState.DONE;
                nextAction = null;
                nextStartAt = null;
                return;
            }

            seasonIndex++;
            roundIndex = 1;

            // generate upcoming season plan now (so /schedule shows upcoming plan during the 1h wait)
            generateSeasonPlan();

            state = RunnerState.WAITING_NEXT_SEASON;
            nextAction = "START_NEXT_SEASON";
            nextStartAt = Instant.now().plus(props.gapBetweenSeasonsMinutes(), ChronoUnit.MINUTES);

            scheduleAt(nextStartAt);
        }
    }

    private void scheduleAt(Instant when) {
        cancelNextTask();
        long delayMs = Math.max(0, Instant.now().until(when, ChronoUnit.MILLIS));
        nextTask = exec.schedule(this::startOrResume, delayMs, TimeUnit.MILLISECONDS);
    }

    private void cancelNextTask() {
        if (nextTask != null) {
            nextTask.cancel(false);
            nextTask = null;
        }
    }
}