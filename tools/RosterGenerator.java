import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Generates synthetic roster JSON files under ./rosters for:
 *  - premier-league.json
 *  - serie-a.json
 *  - la-liga.json
 *
 * <p>Players are synthetic (23 per team) but team names are real club names.
 * JSON schema matches the simulator's LeagueRoster/Team/Player records.</p>
 */
public final class RosterGenerator {

    public static void main(String[] args) throws Exception {
        Path outDir = Path.of("./rosters");
        Files.createDirectories(outDir);

        writePremierLeague(outDir.resolve("premier-league.json"));
        writeSerieA(outDir.resolve("serie-a.json"));
        writeLaLiga(outDir.resolve("la-liga.json"));

        System.out.println("Done. Files written to: " + outDir.toAbsolutePath());
    }

    private static void writePremierLeague(Path outFile) throws Exception {
        // Premier League 2025/26 clubs list [[1]]
        List<TeamSpec> teams = List.of(
                new TeamSpec("pl-ars", "Arsenal", "ARS", 84),
                new TeamSpec("pl-ast", "Aston Villa", "AVL", 80),
                new TeamSpec("pl-bou", "Bournemouth", "BOU", 74),
                new TeamSpec("pl-bre", "Brentford", "BRE", 75),
                new TeamSpec("pl-bha", "Brighton & Hove Albion", "BHA", 77),
                new TeamSpec("pl-bur", "Burnley", "BUR", 72),
                new TeamSpec("pl-che", "Chelsea", "CHE", 82),
                new TeamSpec("pl-cry", "Crystal Palace", "CRY", 75),
                new TeamSpec("pl-eve", "Everton", "EVE", 73),
                new TeamSpec("pl-ful", "Fulham", "FUL", 74),
                new TeamSpec("pl-lee", "Leeds United", "LEE", 73),
                new TeamSpec("pl-liv", "Liverpool", "LIV", 85),
                new TeamSpec("pl-mci", "Manchester City", "MCI", 86),
                new TeamSpec("pl-mun", "Manchester United", "MUN", 81),
                new TeamSpec("pl-new", "Newcastle United", "NEW", 79),
                new TeamSpec("pl-nfo", "Nottingham Forest", "NFO", 72),
                new TeamSpec("pl-sun", "Sunderland", "SUN", 71),
                new TeamSpec("pl-tot", "Tottenham Hotspur", "TOT", 82),
                new TeamSpec("pl-whu", "West Ham United", "WHU", 76),
                new TeamSpec("pl-wol", "Wolverhampton Wanderers", "WOL", 74)
        );

        writeRoster(outFile, "PREMIER_LEAGUE", "SIM-2025-2026", teams);
        System.out.println("Wrote: " + outFile.toAbsolutePath());
    }

    private static void writeSerieA(Path outFile) throws Exception {
        // Serie A 2025/26 clubs list [[2]]
        List<TeamSpec> teams = List.of(
                new TeamSpec("sa-ata", "Atalanta", "ATA", 80),
                new TeamSpec("sa-bol", "Bologna", "BOL", 78),
                new TeamSpec("sa-cag", "Cagliari", "CAG", 72),
                new TeamSpec("sa-com", "Como", "COM", 73),
                new TeamSpec("sa-cre", "Cremonese", "CRE", 71),
                new TeamSpec("sa-fio", "Fiorentina", "FIO", 79),
                new TeamSpec("sa-gen", "Genoa", "GEN", 73),
                new TeamSpec("sa-hel", "Hellas Verona", "VER", 72),
                new TeamSpec("sa-int", "Inter", "INT", 86),
                new TeamSpec("sa-juv", "Juventus", "JUV", 84),
                new TeamSpec("sa-laz", "Lazio", "LAZ", 80),
                new TeamSpec("sa-lec", "Lecce", "LEC", 71),
                new TeamSpec("sa-mil", "AC Milan", "MIL", 83),
                new TeamSpec("sa-nap", "Napoli", "NAP", 84),
                new TeamSpec("sa-par", "Parma", "PAR", 72),
                new TeamSpec("sa-pis", "Pisa", "PIS", 70),
                new TeamSpec("sa-rom", "Roma", "ROM", 82),
                new TeamSpec("sa-sas", "Sassuolo", "SAS", 73),
                new TeamSpec("sa-tor", "Torino", "TOR", 74),
                new TeamSpec("sa-udi", "Udinese", "UDI", 74)
        );

        writeRoster(outFile, "SERIE_A", "SIM-2025-2026", teams);
        System.out.println("Wrote: " + outFile.toAbsolutePath());
    }

    private static void writeLaLiga(Path outFile) throws Exception {
        // LaLiga EA SPORTS 2025/26 clubs list [[3]]
        List<TeamSpec> teams = List.of(
                new TeamSpec("ll-ath", "Athletic Club", "ATH", 78),
                new TeamSpec("ll-atm", "Atlético de Madrid", "ATM", 84),
                new TeamSpec("ll-osa", "CA Osasuna", "OSA", 73),
                new TeamSpec("ll-cel", "Celta", "CEL", 74),
                new TeamSpec("ll-ala", "Deportivo Alavés", "ALA", 72),
                new TeamSpec("ll-elc", "Elche CF", "ELC", 72),
                new TeamSpec("ll-bar", "FC Barcelona", "BAR", 86),
                new TeamSpec("ll-get", "Getafe CF", "GET", 73),
                new TeamSpec("ll-gir", "Girona FC", "GIR", 76),
                new TeamSpec("ll-lev", "Levante UD", "LEV", 71),
                new TeamSpec("ll-ray", "Rayo Vallecano", "RAY", 73),
                new TeamSpec("ll-esp", "RCD Espanyol de Barcelona", "ESP", 72),
                new TeamSpec("ll-mal", "RCD Mallorca", "MLL", 73),
                new TeamSpec("ll-bet", "Real Betis", "BET", 78),
                new TeamSpec("ll-rma", "Real Madrid", "RMA", 87),
                new TeamSpec("ll-ovi", "Real Oviedo", "OVI", 70),
                new TeamSpec("ll-rso", "Real Sociedad", "RSO", 78),
                new TeamSpec("ll-sev", "Sevilla FC", "SEV", 76),
                new TeamSpec("ll-val", "Valencia CF", "VAL", 75),
                new TeamSpec("ll-vil", "Villarreal CF", "VIL", 79)
        );

        writeRoster(outFile, "LA_LIGA", "SIM-2025-2026", teams);
        System.out.println("Wrote: " + outFile.toAbsolutePath());
    }

    private static void writeRoster(Path outFile, String league, String season, List<TeamSpec> teams) throws Exception {
        try (BufferedWriter w = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            JsonWriter jw = new JsonWriter(w, 2);

            jw.beginObject();
            jw.name("league").value(league);
            jw.name("season").value(season);

            jw.name("teams").beginArray();
            for (TeamSpec t : teams) {
                writeTeam(jw, t);
            }
            jw.endArray();

            jw.endObject();
            jw.newline();
        }
    }

    private static void writeTeam(JsonWriter jw, TeamSpec spec) throws Exception {
        int overall = spec.overall;
        int attack = clamp(overall + 1, 60, 90);
        int midfield = clamp(overall, 60, 90);
        int defense = clamp(overall - 1, 60, 90);
        int keeper = clamp(overall - 2, 60, 90);
        int discipline = clamp(50 + (80 - overall), 40, 65);

        jw.beginObject();
        jw.name("teamId").value(spec.teamId);
        jw.name("name").value(spec.name);
        jw.name("shortName").value(spec.shortName);

        jw.name("strength").beginObject();
        jw.name("attack").value(attack);
        jw.name("midfield").value(midfield);
        jw.name("defense").value(defense);
        jw.name("keeper").value(keeper);
        jw.name("discipline").value(discipline);
        jw.endObject();

        jw.name("players").beginArray();
        writePlayers23(jw, spec.teamId, spec.name, overall);
        jw.endArray();

        jw.endObject();
    }

    /**
     * 23-player squad distribution:
     *  - 3 GK
     *  - 8 DEF (FB/CB)
     *  - 8 MID (DM/CM/AM/W mix)
     *  - 4 FWD (W/ST)
     */
    private static void writePlayers23(JsonWriter jw, String teamId, String teamName, int overall) throws Exception {
        PlayerPlan[] plan = new PlayerPlan[] {
                new PlayerPlan("GK", 1,  -2, 60, 72),
                new PlayerPlan("GK", 13, -5, 62, 70),
                new PlayerPlan("GK", 31, -7, 64, 68),

                new PlayerPlan("FB", 2,  -4, 58, 78),
                new PlayerPlan("FB", 3,  -4, 58, 78),
                new PlayerPlan("CB", 4,  -3, 60, 76),
                new PlayerPlan("CB", 5,  -3, 60, 76),
                new PlayerPlan("CB", 6,  -5, 61, 75),
                new PlayerPlan("FB", 12, -6, 59, 76),
                new PlayerPlan("FB", 22, -7, 59, 75),
                new PlayerPlan("CB", 23, -7, 61, 74),

                new PlayerPlan("CM", 8,  -1, 54, 82),
                new PlayerPlan("AM", 10,  0,  52, 80),
                new PlayerPlan("CM", 14, -3, 55, 80),
                new PlayerPlan("DM", 16, -2, 57, 81),
                new PlayerPlan("W",  18, -3, 52, 79),
                new PlayerPlan("CM", 20, -6, 56, 78),
                new PlayerPlan("DM", 24, -7, 58, 77),
                new PlayerPlan("AM", 25, -7, 54, 77),

                new PlayerPlan("W",  7,   0,  50, 79),
                new PlayerPlan("ST", 9,   0,  49, 78),
                new PlayerPlan("W",  11, -2,  50, 79),
                new PlayerPlan("ST", 15, -4,  51, 76)
        };

        for (int i = 0; i < plan.length; i++) {
            PlayerPlan p = plan[i];
            int rating = clamp(overall + p.ratingDelta, 60, 90);

            jw.beginObject();
            jw.name("playerId").value(teamId + "-" + twoDigits(i + 1));
            jw.name("name").value(teamName + " Player " + (i + 1));
            jw.name("position").value(p.position);
            jw.name("shirt").value(p.shirt);
            jw.name("rating").value(rating);
            jw.name("discipline").value(p.discipline);
            jw.name("stamina").value(p.stamina);
            jw.endObject();
        }
    }

    private static String twoDigits(int n) {
        return (n < 10) ? ("0" + n) : String.valueOf(n);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private record TeamSpec(String teamId, String name, String shortName, int overall) {}
    private record PlayerPlan(String position, int shirt, int ratingDelta, int discipline, int stamina) {}

    /**
     * Minimal JSON writer with indentation and correct comma handling for objects/arrays.
     */
    private static final class JsonWriter {
        private final BufferedWriter w;
        private final int indentSize;
        private int indent = 0;

        private final Deque<Ctx> stack = new ArrayDeque<>();
        private boolean afterName = false;

        private enum Kind { OBJECT, ARRAY }
        private static final class Ctx {
            final Kind kind;
            boolean first = true;
            Ctx(Kind kind) { this.kind = kind; }
        }

        JsonWriter(BufferedWriter w, int indentSize) {
            this.w = w;
            this.indentSize = indentSize;
        }

        void beginObject() throws Exception {
            beforeValue();
            w.write("{");
            stack.push(new Ctx(Kind.OBJECT));
            indent++;
            newline();
        }

        void endObject() throws Exception {
            indent--;
            newline();
            writeIndent();
            w.write("}");
            stack.pop();
            afterName = false;
        }

        void beginArray() throws Exception {
            beforeValue();
            w.write("[");
            stack.push(new Ctx(Kind.ARRAY));
            indent++;
            newline();
        }

        void endArray() throws Exception {
            indent--;
            newline();
            writeIndent();
            w.write("]");
            stack.pop();
            afterName = false;
        }

        JsonWriter name(String name) throws Exception {
            if (stack.isEmpty() || stack.peek().kind != Kind.OBJECT) {
                throw new IllegalStateException("name() only valid inside an object");
            }
            Ctx ctx = stack.peek();
            if (!ctx.first) {
                w.write(",");
                newline();
            }
            ctx.first = false;
            writeIndent();
            w.write("\"" + escape(name) + "\": ");
            afterName = true;
            return this;
        }

        JsonWriter value(String s) throws Exception {
            beforeValue();
            w.write("\"" + escape(s) + "\"");
            return this;
        }

        JsonWriter value(int n) throws Exception {
            beforeValue();
            w.write(Integer.toString(n));
            return this;
        }

        void newline() throws Exception {
            w.write("\n");
        }

        private void beforeValue() throws Exception {
            if (afterName) {
                afterName = false;
                return;
            }
            if (!stack.isEmpty() && stack.peek().kind == Kind.ARRAY) {
                Ctx ctx = stack.peek();
                if (!ctx.first) {
                    w.write(",");
                    newline();
                }
                ctx.first = false;
                writeIndent();
            }
        }

        private void writeIndent() throws Exception {
            for (int i = 0; i < indent * indentSize; i++) w.write(" ");
        }

        private String escape(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}