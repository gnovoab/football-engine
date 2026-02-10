package com.gnovoa.football.core;
import com.gnovoa.football.events.*;
import com.gnovoa.football.model.League;
import com.gnovoa.football.model.Team;
import com.gnovoa.football.rosters.SimProperties;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory state for a single football match.
 *
 * <p>This class owns:
 * <ul>
 *   <li>Match clock and phase transitions (1st half → half-time → 2nd half → full-time)</li>
 *   <li>Score and lightweight statistics used in {@link FootballEventType#MATCH_SNAPSHOT}</li>
 *   <li>Stoppage time accumulation (simplified MVP)</li>
 * </ul>
 *
 * <p>It deliberately does not handle persistence (no DB) and is designed to be advanced
 * by {@link MatchEngine} in real time.
 */
public final class MatchState {

    private final League league;
    private final String seasonId;
    private final String fixtureId;
    private final String matchId;

    private final Team home;
    private final Team away;

    private MatchPhase phase = MatchPhase.FIRST_HALF;

    /** Playing time seconds since kickoff (includes stoppage windows once added). */
    private int matchSecond = 0;

    /** Current stoppage second indicator (reserved for future; kept for UI payload compatibility). */
    private int stoppageSecond = 0;

    private int homeScore = 0;
    private int awayScore = 0;

    /** Half-time real-world elapsed seconds (half-time is compressed to N seconds). */
    private int halfTimeRealSeconds = 0;

    private final int halfTimeRealSecondsConfig;

    /** Accumulated stoppage time for the first and second halves (seconds). */
    private int stoppageFirstHalfSeconds = 0;
    private int stoppageSecondHalfSeconds = 0;

    /** Counter used to emit snapshots every {@code snapshotEverySeconds}. */
    private int snapshotCounter = 0;

    private final int snapshotEverySeconds;

    // lightweight stats for snapshots
    private int shotsHome = 0, shotsAway = 0, sotHome = 0, sotAway = 0, cornersHome = 0, cornersAway = 0;

    /**
     * Creates a new match state.
     *
     * @param league league identifier
     * @param seasonId season identifier for this runtime season (e.g. "season-1")
     * @param fixtureId fixture identifier for this round
     * @param matchId unique match identifier
     * @param home home team
     * @param away away team
     * @param simProps simulation configuration (half-time duration, snapshot interval)
     */
    public MatchState(League league, String seasonId, String fixtureId, String matchId, Team home, Team away, SimProperties simProps) {
        this.league = league;
        this.seasonId = seasonId;
        this.fixtureId = fixtureId;
        this.matchId = matchId;
        this.home = home;
        this.away = away;
        this.halfTimeRealSecondsConfig = simProps.halfTimeRealSeconds();
        this.snapshotEverySeconds = simProps.snapshotEverySeconds();
    }

    public League league() { return league; }
    public String seasonId() { return seasonId; }
    public String fixtureId() { return fixtureId; }
    public String matchId() { return matchId; }
    public Team home() { return home; }
    public Team away() { return away; }

    public MatchPhase phase() { return phase; }

    /** @return true when the match reached full time. */
    public boolean isFinished() { return phase == MatchPhase.FINISHED; }

    /** @return true only during 1st or 2nd half. */
    public boolean isPlaying() { return phase == MatchPhase.FIRST_HALF || phase == MatchPhase.SECOND_HALF; }

    /** @return current playing time in seconds. */
    public int matchSecond() { return matchSecond; }

    /**
     * Adds stoppage time based on incidents (subs, VAR, injuries, etc.).
     * MVP behavior: allocates seconds to the half currently being played.
     *
     * @param s seconds to add
     */
    public void addStoppageSeconds(int s) {
        if (phase == MatchPhase.FIRST_HALF) stoppageFirstHalfSeconds += s;
        else if (phase == MatchPhase.SECOND_HALF) stoppageSecondHalfSeconds += s;
    }

    public void goalHome() { homeScore++; }
    public void goalAway() { awayScore++; }

    public void shotHome(boolean onTarget) { shotsHome++; if (onTarget) sotHome++; }
    public void shotAway(boolean onTarget) { shotsAway++; if (onTarget) sotAway++; }
    public void cornerHome() { cornersHome++; }
    public void cornerAway() { cornersAway++; }

    /**
     * Creates a snapshot of the current match clock and score.
     *
     * @return snapshot used in all events for UI convenience
     */
    public MatchClockSnapshot snapshot() {
        int minute = matchSecond / 60;
        return new MatchClockSnapshot(
                phase.name(),
                matchSecond,
                minute,
                stoppageSecond,
                homeScore,
                awayScore
        );
    }

    /**
     * Creates an event stamped with {@link Instant#now()} and the current {@link #snapshot()}.
     *
     * @param type event type
     * @param data event payload
     * @return match event ready to publish
     */
    public MatchEvent eventNow(FootballEventType type, Map<String, Object> data) {
        return new MatchEvent(league, seasonId, fixtureId, matchId, Instant.now(), snapshot(), type, data);
    }

    /**
     * Advances the match clock by one real-time second.
     *
     * <p>This may:
     * <ul>
     *   <li>Transition phases and emit phase events (HALF_TIME, SECOND_HALF_KICK_OFF, FULL_TIME)</li>
     *   <li>Emit a periodic {@link FootballEventType#MATCH_SNAPSHOT} event every N seconds</li>
     * </ul>
     *
     * @return optional event generated by clock/phase/snapshot (at most one per second in MVP)
     */
    public Optional<MatchEvent> advanceOneSecond() {
        if (phase == MatchPhase.FINISHED) return Optional.empty();

        if (phase == MatchPhase.HALF_TIME) {
            halfTimeRealSeconds++;
            if (halfTimeRealSeconds >= halfTimeRealSecondsConfig) {
                phase = MatchPhase.SECOND_HALF;
                return Optional.of(eventNow(FootballEventType.SECOND_HALF_KICK_OFF, Map.of()));
            }
            // no snapshot emission during half-time in MVP tick; can be enabled if desired
            return Optional.empty();
        }

        // playing
        matchSecond++;

        int firstHalfTarget = 45 * 60 + stoppageFirstHalfSeconds;
        int secondHalfTarget = 90 * 60 + stoppageSecondHalfSeconds;

        if (phase == MatchPhase.FIRST_HALF && matchSecond >= firstHalfTarget) {
            phase = MatchPhase.HALF_TIME;
            halfTimeRealSeconds = 0;
            return Optional.of(eventNow(FootballEventType.HALF_TIME, Map.of()));
        }

        if (phase == MatchPhase.SECOND_HALF && matchSecond >= secondHalfTarget) {
            phase = MatchPhase.FINISHED;
            return Optional.of(eventNow(FootballEventType.FULL_TIME, Map.of()));
        }

        snapshotCounter++;
        if (snapshotCounter >= snapshotEverySeconds) {
            snapshotCounter = 0;
            return Optional.of(eventNow(
                    FootballEventType.MATCH_SNAPSHOT,
                    Map.of(
                            "stats", Map.of(
                                    "shotsHome", shotsHome, "shotsAway", shotsAway,
                                    "sotHome", sotHome, "sotAway", sotAway,
                                    "cornersHome", cornersHome, "cornersAway", cornersAway
                            )
                    )
            ));
        }

        return Optional.empty();
    }
}