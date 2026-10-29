package com.auditchain.web;

import com.auditchain.scenario.ClimateDisruptionScenarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {
    private final ClimateDisruptionScenarioService scenarioService;

    public ScenarioController(ClimateDisruptionScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @PostMapping("/climate-disruption")
    public ResponseEntity<?> climateDisruption(
            @RequestParam(defaultValue = "120") int outageMinutes,
            @RequestParam(defaultValue = "12") int affectedUnits,
            @RequestParam(defaultValue = "240") int queuedSubmissions) {
        try {
            return ResponseEntity.ok(
                    scenarioService.run(outageMinutes, affectedUnits, queuedSubmissions));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
