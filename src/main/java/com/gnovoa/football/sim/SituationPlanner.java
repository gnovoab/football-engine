package com.gnovoa.football.sim;
import com.gnovoa.football.core.MatchState;
import com.gnovoa.football.events.FootballEventType;
import com.gnovoa.football.events.MatchEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SituationPlanner {

    private final RandomSource rnd;

    public SituationPlanner(RandomSource rnd) {
        this.rnd = rnd;
    }

    public List<MatchEvent> maybeGenerate(MatchState s) {
        if (!s.isPlaying()) return List.of();

        // very rough hazard per second
        double shotChance = 0.006;     // ~ 0.6% per second => ~ 20 shots per match-ish
        double foulChance = 0.004;     // not fully implemented here
        double varChance  = 0.00025;

        double p = rnd.nextDouble();
        List<MatchEvent> out = new ArrayList<>();

        if (p < varChance) {
            out.add(s.eventNow(FootballEventType.VAR_CHECK, Map.of()));
            if (rnd.nextDouble() < 0.10) { // rare overturn
                out.add(s.eventNow(FootballEventType.VAR_OVERTURNED, Map.of("reason", "INCIDENT_REVIEW")));
                s.addStoppageSeconds(20);
            } else {
                s.addStoppageSeconds(10);
            }
            return out;
        }

        if (p < shotChance) {
            boolean homeAttacks = rnd.nextDouble() < 0.5;
            boolean onTarget = rnd.nextDouble() < 0.35;

            if (homeAttacks) s.shotHome(onTarget); else s.shotAway(onTarget);

            out.add(s.eventNow(onTarget ? FootballEventType.SHOT_ON_TARGET : FootballEventType.SHOT_OFF_TARGET,
                    Map.of("team", homeAttacks ? "HOME" : "AWAY")));

            if (onTarget) {
                boolean goal = rnd.nextDouble() < 0.12; // ~12% of on-target shots become goals
                if (goal) {
                    if (homeAttacks) s.goalHome(); else s.goalAway();
                    out.add(s.eventNow(FootballEventType.GOAL, Map.of(
                            "team", homeAttacks ? "HOME" : "AWAY",
                            "score", Map.of("home", s.snapshot().homeScore(), "away", s.snapshot().awayScore())
                    )));
                    s.addStoppageSeconds(15);
                } else {
                    out.add(s.eventNow(FootballEventType.SAVE, Map.of("by", "GK")));
                    if (rnd.nextDouble() < 0.25) { // corner after save/deflection
                        if (homeAttacks) s.cornerHome(); else s.cornerAway();
                        out.add(s.eventNow(FootballEventType.CORNER_KICK, Map.of("team", homeAttacks ? "HOME" : "AWAY")));
                    }
                }
            }
            return out;
        }

        if (p < shotChance + foulChance) {
            // MVP: occasionally issue a card
            if (rnd.nextDouble() < 0.18) {
                out.add(s.eventNow(FootballEventType.YELLOW_CARD, Map.of("team", rnd.nextDouble() < 0.5 ? "HOME" : "AWAY")));
                s.addStoppageSeconds(10);
            }
            return out;
        }

        // substitutions: more likely late game
        int minute = s.matchSecond() / 60;
        if (minute >= 55 && rnd.nextDouble() < 0.0008) {
            out.add(s.eventNow(FootballEventType.SUBSTITUTION, Map.of("team", rnd.nextDouble() < 0.5 ? "HOME" : "AWAY")));
            s.addStoppageSeconds(8);
            return out;
        }

        return List.of();
    }
}