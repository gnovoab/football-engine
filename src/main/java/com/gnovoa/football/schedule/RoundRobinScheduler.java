package com.gnovoa.football.schedule;

import com.gnovoa.football.model.Team;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates league fixtures for 20-team competitions.
 *
 * <p>Provides:
 * <ul>
 *   <li>Single round-robin (19 rounds)</li>
 *   <li>Double round-robin (38 rounds) with the second leg home/away swapped</li>
 *   <li>Second-leg reordering to avoid immediate rematches between rounds 19 and 20</li>
 * </ul>
 *
 * <p>Note: This scheduler is deterministic in structure but may use randomization when
 * reordering the second leg. This is acceptable because match simulation itself is non-replayable.
 */
public final class RoundRobinScheduler {

    /** A match pairing within a fixture (round). */
    public record Pairing(Team home, Team away) {}

    /** A fixture (round) containing exactly 10 matches for a 20-team league. */
    public record Fixture(int roundIndex, List<Pairing> matches) {}

    /**
     * Creates a double round-robin schedule (38 rounds) and reorders second-leg rounds.
     *
     * <p>Second leg is built by swapping home/away of the first leg and then reshuffled to avoid
     * the same opponent being faced immediately across the 19→20 boundary.
     *
     * @param teams list of exactly 20 teams
     * @return list of 38 fixtures, each containing 10 pairings
     */
    public List<Fixture> doubleRoundRobinReshuffledSecondLeg(List<Team> teams) {
        List<Fixture> firstLeg = singleRoundRobin(teams); // 19

        List<Fixture> secondLegBase = new ArrayList<>(19);
        for (Fixture f : firstLeg) {
            List<Pairing> flipped = f.matches().stream()
                    .map(p -> new Pairing(p.away(), p.home()))
                    .toList();
            secondLegBase.add(new Fixture(f.roundIndex(), flipped));
        }

        List<Fixture> secondLegReordered = reorderSecondLegAvoidImmediateRematches(firstLeg, secondLegBase);

        List<Fixture> all = new ArrayList<>(38);
        all.addAll(firstLeg);
        for (int i = 0; i < 19; i++) {
            all.add(new Fixture(20 + i, secondLegReordered.get(i).matches()));
        }
        return all;
    }

    /**
     * Reorders second-leg rounds to avoid immediate rematches between the last first-leg round
     * and the first second-leg round.
     *
     * <p>This is a lightweight realism enhancement. It does not attempt to optimize other
     * constraints (home/away streaks, rematches within N rounds, etc.).
     */
    private List<Fixture> reorderSecondLegAvoidImmediateRematches(List<Fixture> firstLeg, List<Fixture> secondLegBase) {
        Fixture lastFirstLeg = firstLeg.get(firstLeg.size() - 1);
        Map<String, String> lastOpponents = opponentsByTeamId(lastFirstLeg);

        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < 19; i++) idx.add(i);

        for (int attempt = 0; attempt < 200; attempt++) {
            Collections.shuffle(idx, ThreadLocalRandom.current());
            Fixture first = secondLegBase.get(idx.get(0));
            if (hasAnyImmediateRematch(lastOpponents, first)) continue;

            List<Fixture> ordered = new ArrayList<>(19);
            for (int i : idx) ordered.add(secondLegBase.get(i));
            return ordered;
        }

        // Fallback: rotate to reduce chance of immediate rematch if shuffle failed repeatedly
        List<Fixture> fb = new ArrayList<>(19);
        fb.addAll(secondLegBase.subList(1, 19));
        fb.add(secondLegBase.get(0));
        return fb;
    }

    private boolean hasAnyImmediateRematch(Map<String, String> lastOpponents, Fixture nextFixture) {
        Map<String, String> nextOpp = opponentsByTeamId(nextFixture);
        for (var e : lastOpponents.entrySet()) {
            if (Objects.equals(e.getValue(), nextOpp.get(e.getKey()))) return true;
        }
        return false;
    }

    private Map<String, String> opponentsByTeamId(Fixture fixture) {
        Map<String, String> map = new HashMap<>();
        for (Pairing p : fixture.matches()) {
            map.put(p.home().teamId(), p.away().teamId());
            map.put(p.away().teamId(), p.home().teamId());
        }
        return map;
    }

    /**
     * Generates a single round-robin schedule for 20 teams using the circle method.
     *
     * @param teams list of exactly 20 teams
     * @return 19 fixtures (rounds), each with 10 pairings
     *
     * @throws IllegalArgumentException if teams size is not 20
     */
    public List<Fixture> singleRoundRobin(List<Team> teams) {
        if (teams.size() != 20) throw new IllegalArgumentException("Expected 20 teams");

        List<Team> list = new ArrayList<>(teams);
        Team fixed = list.remove(0);
        int n = list.size(); // 19

        List<Fixture> fixtures = new ArrayList<>(19);

        for (int round = 0; round < 19; round++) {
            List<Team> left = new ArrayList<>();
            List<Team> right = new ArrayList<>();

            left.add(fixed);
            left.addAll(list.subList(0, n / 2));

            right.addAll(list.subList(n / 2, n));
            Collections.reverse(right);

            List<Pairing> pairings = new ArrayList<>(10);
            for (int i = 0; i < 10; i++) {
                Team a = left.get(i);
                Team b = right.get(i);
                boolean flip = (round % 2 == 1);
                pairings.add(flip ? new Pairing(b, a) : new Pairing(a, b));
            }

            fixtures.add(new Fixture(round + 1, pairings));

            Team last = list.remove(list.size() - 1);
            list.add(0, last);
        }

        return fixtures;
    }
}