package com.gnovoa.football.rosters;

import com.gnovoa.football.model.League;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sim")
public record SimProperties(
    int tickMillis, int snapshotEverySeconds, int halfTimeRealSeconds, Rosters rosters) {
  public record Rosters(String baseDir, Map<League, String> files) {
    public Rosters {
      if (files == null) files = new EnumMap<>(League.class);
    }
  }
}
