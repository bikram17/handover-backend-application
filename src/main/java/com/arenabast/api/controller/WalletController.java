package com.arenabast.api.controller;

import com.arenabast.api.auth.jwt.UserContext;
import com.arenabast.api.dto.*;
import com.arenabast.api.enums.TransactionApprovalStatus;
import com.arenabast.api.service.WalletService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController extends ApiRestHandler {

    private final WalletService walletService;
    private final UserContext userContext;

    public WalletController(WalletService walletService, UserContext userContext) {
        this.walletService = walletService;
        this.userContext = userContext;
    }

    @GetMapping("/view")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'AGENT')")
    public ResponseWrapper<List<WalletViewDto>> viewWallets(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long agentId
    ) {
        List<WalletViewDto> wallets = walletService.getWalletsBasedOnRole(role, agentId);
        return new ResponseWrapper<>(200, "Wallets fetched", wallets);
    }

    @PostMapping("/management/add-cash")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public ResponseWrapper<String> addCashToManagementWallet(@RequestBody AddBalanceRequest request) {
        walletService.addCashToManagementWallet(request);
        return new ResponseWrapper<>(200, "Cash added to management wallet", null);
    }

    @PostMapping("/player/add-cash")
    @PreAuthorize("hasAuthority('AGENT')")
    public ResponseWrapper<String> playerGivesCash(@RequestBody AddPlayerBalanceRequest request) {
        walletService.addCashToPlayerByAgent(request);
        return new ResponseWrapper<>(200, "Cash received from player and added to wallet", null);
    }

    @PostMapping("/settlement/agent-to-admin/initiate")
    @PreAuthorize("hasAuthority('AGENT')")
    public ResponseWrapper<String> initiateAgentToAdminSettlement(@RequestBody AgentToAdminSettlementRequest req) {
        walletService.initiateAgentToAdminSettlement(req);
        return new ResponseWrapper<>(200, "Settlement request sent to admin", null);
    }

    @PostMapping("/settlement/agent-to-admin/approve/{requestId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseWrapper<String> approveAgentToAdminSettlement(@PathVariable Long requestId) {
        walletService.approveAgentToAdminSettlement(requestId);
        return new ResponseWrapper<>(200, "Settlement approved", null);
    }

    @PostMapping("/settlement/admin-to-superadmin/initiate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseWrapper<String> initiateAdminToSuperAdminSettlement(@RequestBody AdminToSuperAdminSettlementRequest req) {
        walletService.initiateAdminToSuperAdminSettlement(req);
        return new ResponseWrapper<>(200, "Settlement request sent to Super Admin", null);
    }

    @PostMapping("/settlement/admin-to-superadmin/approve/{requestId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseWrapper<String> approveAdminToSuperAdminSettlement(@PathVariable Long requestId) {
        walletService.approveAdminToSuperAdminSettlement(requestId);
        return new ResponseWrapper<>(200, "Settlement approved", null);
    }

    @GetMapping("/settlement/requests/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseWrapper<List<SettlementRequestDto>> getIncomingSettlementRequests() {
        Long userId = userContext.getUserId();
        List<SettlementRequestDto> dtos = walletService.getPendingSettlementRequestsForUser(userId);
        return new ResponseWrapper<>(200, "Pending settlement requests", dtos);
    }

    @GetMapping("/settlement/requests/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseWrapper<SettlementRequestsSummaryDto> getAllSettlementRequests(@RequestParam(required = false) TransactionApprovalStatus filter) {
        Long userId = userContext.getUserId();
        SettlementRequestsSummaryDto summary = walletService.getAllRequestsSummaryForUser(userId, filter);
        return new ResponseWrapper<>(200, "Settlement requests summary", summary);
    }

    @PostMapping("/settlement/requests/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseWrapper<String> approveSettlement(@PathVariable Long id, @RequestParam boolean status) {
        walletService.processSettlement(status, id);
        return new ResponseWrapper<>(200, "Settlement approved", null);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('AGENT', 'ADMIN', 'SUPER_ADMIN', 'PLAYER')")
    public ResponseWrapper<ResponseDto> getMyWalletDetails() {
        ResponseDto wallet = walletService.getMyWalletOverview();
        return new ResponseWrapper<>(200, "Fetched wallet details", wallet);
    }
}
