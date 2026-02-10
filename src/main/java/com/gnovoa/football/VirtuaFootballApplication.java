// Namespace
package com.gnovoa.football;

// Imports
import com.gnovoa.football.rosters.SimProperties;
import com.gnovoa.football.runner.RunnerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** The Main app */
@SpringBootApplication
@EnableConfigurationProperties({SimProperties.class, RunnerProperties.class})
public class VirtuaFootballApplication {

  public static void main(String[] args) {
    SpringApplication.run(VirtuaFootballApplication.class, args);
  }
}
