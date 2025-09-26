package com.arenabast.api.controller;


import com.arenabast.api.dto.BetFilterRequest;
import com.arenabast.api.dto.PlaceBetRequest;
import com.arenabast.api.dto.ResponseWrapper;
import com.arenabast.api.entity.BetEntity;
import com.arenabast.api.enums.BetResult;
import com.arenabast.api.service.BetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/app/bet")
@Slf4j
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PLAYER')")
public class BetsController extends ApiRestHandler {


    private final BetService betService;

    public BetsController(BetService betService) {
        super();
        this.betService = betService;
    }

    @PreAuthorize("hasAuthority('PLAYER')")
    @PostMapping("/{playerId}/bets")
    public ResponseWrapper<BetEntity> placeBet(
            @PathVariable Long playerId,
            @RequestBody PlaceBetRequest request
    ) {
        return new ResponseWrapper<>(true, 200, betService.placeBet(playerId, request));
    }

    @PreAuthorize("hasAuthority('PLAYER')")
    @GetMapping("/{playerId}/bets")
    public ResponseWrapper<List<BetEntity>> getPlayerBets(@PathVariable Long playerId) {
        return new ResponseWrapper<>(true, 200, betService.getPlayerBets(playerId));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping("/bets/{betId}/settle")
    public ResponseWrapper<BetEntity> settleBet(
            @PathVariable Long betId,
            @RequestParam BetResult result,
            @RequestParam Long playerId
    ) {
        return new ResponseWrapper<>(true, 200, betService.settleBet(betId, result, playerId));
    }

    @GetMapping("/bets/all")
    public ResponseWrapper<Page<BetEntity>> getAllBets(
            BetFilterRequest filter,
            Pageable pageable
    ) {
        return new ResponseWrapper<>(true, 200, betService.getAllBets(filter, pageable));
    }
}
