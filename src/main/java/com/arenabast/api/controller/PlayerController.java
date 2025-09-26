package com.arenabast.api.controller;

import com.arenabast.api.dto.*;
import com.arenabast.api.entity.PlayerEntity;
import com.arenabast.api.enums.TransactionType;
import com.arenabast.api.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/player")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'AGENT')")
public class PlayerController extends ApiRestHandler {

    private final PlayerService playerService;

    @PostMapping("/create")
    public ResponseWrapper<PlayerEntity> createPlayer(@RequestBody @Valid CreatePlayerRequest request) {
        return new ResponseWrapper<>(true, 200, playerService.createPlayer(request));
    }

    @PostMapping("/{playerId}/wallet/request")
    public ResponseEntity<WalletTransactionRequestEntity> requestWalletAction(
            @PathVariable Long playerId,
            @RequestParam TransactionType type,
            @RequestParam Double amount,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(playerService.requestWalletAction(playerId, type, amount, note));
    }

    @PreAuthorize("hasAnyAuthority('AGENT')")
    @PostMapping("/{playerId}/wallet/topUp")
    public ResponseEntity<WalletTransactionRequestEntity> topUpWalletAction(
            @PathVariable Long playerId,
            @RequestParam TransactionType type,
            @RequestParam Double amount,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(playerService.topUpWallet(playerId, type, amount, note));
    }

    @PostMapping("/{playerId}/wallet/confirm/{requestId}")
    public ResponseEntity<Void> confirmWithdrawalReceived(
            @PathVariable Long playerId,
            @PathVariable Long requestId) {
        playerService.confirmWithdrawalReceived(requestId, playerId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'AGENT')")
    @GetMapping
    public ResponseEntity<ResponseWrapper<PaginatedResponse<PlayerDto>>> getFilteredPlayers(
            @RequestParam(required = false) Long agentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean active) {

        PaginatedResponse<PlayerDto> players = playerService.getFilteredPlayers(agentId, active, page, size);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, players));
    }

    @GetMapping("/{playerId}/get-profile")
    public ResponseWrapper<PlayerProfileDto> getPlayerProfile(@PathVariable Long playerId) {
        PlayerProfileDto profile = playerService.getPlayerProfile(playerId);
        return new ResponseWrapper<>(200, "Fetched player profile", profile);
    }

    @PutMapping("/{playerId}/edit")
    public ResponseWrapper<PlayerDto> updatePlayer(
            @PathVariable Long playerId,
            @RequestBody UpdatePlayerRequest request
    ) {
        PlayerDto updated = playerService.updatePlayer(playerId, request);
        return new ResponseWrapper<>(200, "Player updated successfully", updated);
    }

    @PutMapping("/{playerId}/status/{activeStatus}")
    public ResponseWrapper<PlayerDto> updatePlayer(
            @PathVariable Long playerId,
            @PathVariable boolean activeStatus
    ) {
        PlayerDto updated = playerService.updatePlayerStatus(playerId, activeStatus);
        return new ResponseWrapper<>(200, "Player updated successfully", updated);
    }
}