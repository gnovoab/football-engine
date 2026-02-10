package com.gnovoa.football.api;

import com.gnovoa.football.api.dto.LeagueScheduleResponse;
import com.gnovoa.football.api.dto.RunningFixtureResponse;
import com.gnovoa.football.model.League;
import com.gnovoa.football.runner.RunnerFacade;
import com.gnovoa.football.runner.RunnerRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/runners")
public class RunnerController {

    private final RunnerFacade facade;
    private final RunnerRegistry registry;

    public RunnerController(RunnerFacade facade, RunnerRegistry registry) {
        this.facade = facade;
        this.registry = registry;
    }

    @PostMapping("/{league}/pause")
    public ResponseEntity<Void> pause(@PathVariable League league) {
        registry.runner(league).pause();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{league}/stop")
    public ResponseEntity<Void> stop(@PathVariable League league) {
        registry.runner(league).stop(); // immediate
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{league}/resume")
    public ResponseEntity<Void> resume(@PathVariable League league) {
        registry.runner(league).startOrResume();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{league}/running-fixture")
    public RunningFixtureResponse runningFixture(@PathVariable League league) {
        return facade.getRunningFixtureOrNext(league);
    }

    @GetMapping("/{league}/schedule")
    public LeagueScheduleResponse schedule(@PathVariable League league) {
        return facade.getSchedule(league);
    }
}