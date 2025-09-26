package com.arenabast.api.controller;

import com.arenabast.api.dto.SimulateBetRequest;
import com.arenabast.api.entity.TransactionLogEntity;
import com.arenabast.api.service.TransactionSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController extends ApiRestHandler {

    private final TransactionSimulationService simulationService;

    @PostMapping("/simulate")
    public ResponseEntity<TransactionLogEntity> simulateBet(@RequestBody SimulateBetRequest request) {
        return ResponseEntity.ok(simulationService.simulateBetOutcome(request));
    }
}