package com.gnovoa.football.rosters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnovoa.football.model.League;
import com.gnovoa.football.model.LeagueRoster;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Loads and validates league rosters from the local filesystem.
 *
 * <p>Rosters are configured via {@code sim.rosters.baseDir} and {@code sim.rosters.files.*} and are
 * expected to be JSON matching {@link LeagueRoster}.
 *
 * <p>This component keeps all rosters in memory (no DB). It is constructed once at application
 * startup and fails fast if any roster file is missing or invalid.
 */
//@Component
public final class RosterCatalog {

  /** In-memory roster cache by league. */
  private final Map<League, LeagueRoster> rosters = new EnumMap<>(League.class);

  /**
   * Loads all configured rosters into memory.
   *
   * @param mapper Jackson mapper used to deserialize JSON roster files
   * @param props simulation properties (includes roster file locations)
   * @throws IllegalStateException if a roster file cannot be read or parsed
   * @throws IllegalArgumentException if roster content is invalid (wrong league, wrong team count,
   *     etc.)
   */
  public RosterCatalog(ObjectMapper mapper, SimProperties props) {
    Path baseDir = Path.of(props.rosters().baseDir()).toAbsolutePath().normalize();

    props
        .rosters()
        .files()
        .forEach(
            (league, fileName) -> {
              Path p = baseDir.resolve(fileName).normalize();
              try (var in = Files.newInputStream(p)) {
                LeagueRoster roster = mapper.readValue(in, LeagueRoster.class);
                validate(roster, league, p);
                rosters.put(league, roster);
              } catch (Exception e) {
                throw new IllegalStateException(
                    "Failed to load roster " + league + " from " + p, e);
              }
            });
  }

  /**
   * Returns the roster for the given league.
   *
   * @param league league identifier
   * @return loaded roster (never null)
   * @throws IllegalArgumentException if no roster is loaded for the league
   */
  public LeagueRoster roster(League league) {
    LeagueRoster r = rosters.get(league);
    if (r == null) throw new IllegalArgumentException("No roster loaded for " + league);
    return r;
  }

  /**
   * Validates a roster against MVP constraints:
   *
   * <ul>
   *   <li>Roster league must match the expected league
   *   <li>Exactly 20 teams per league
   *   <li>At least 18 players per team
   * </ul>
   *
   * @param roster league roster parsed from JSON
   * @param expectedLeague league this file is mapped to in configuration
   * @param path filesystem path used only for error reporting
   */
  private void validate(LeagueRoster roster, League expectedLeague, Path path) {
    if (roster.league() != expectedLeague) {
      throw new IllegalArgumentException("Roster league mismatch in " + path);
    }
    if (roster.teams() == null || roster.teams().size() != 20) {
      throw new IllegalArgumentException(
          "League " + roster.league() + " must have exactly 20 teams (file " + path + ")");
    }
    roster
        .teams()
        .forEach(
            t -> {
              if (t.players() == null || t.players().size() < 18) {
                throw new IllegalArgumentException(
                    "Team " + t.name() + " must have at least 18 players (file " + path + ")");
              }
            });
  }
}
