package com.arenabast.api.controller;

import com.arenabast.api.auth.jwt.UserContext;
import com.arenabast.api.dto.ResponseWrapper;
import com.arenabast.api.dto.TransactionLogDto;
import com.arenabast.api.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'AGENT')")
public class TransactionQueryController extends ApiRestHandler {

    private final TransactionQueryService queryService;
    private final UserContext userContext;

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'AGENT', 'PLAYER')")
    @GetMapping("/my")
    public ResponseWrapper<Page<TransactionLogDto>> getMyTransactions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        String role = UserContext.getRole();

        int defaultPage = page != null ? page : 0;
        int defaultSize = size != null ? size : 20;

        Pageable pageable = PageRequest.of(defaultPage, defaultSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        if ("SUPER_ADMIN".equals(role)) {
            return new ResponseWrapper<>(200, "Fetched all transactions", queryService.getAll(pageable));
        }

        Long userId = userContext.getUserId();

        if ("ADMIN".equals(role)) {
            return new ResponseWrapper<>(200, "Fetched admin transactions", queryService.getByAdmin(userId, pageable));
        }

        if ("AGENT".equals(role)) {
            return new ResponseWrapper<>(200, "Fetched agent transactions", queryService.getByAgent(userId, pageable));
        }

        if ("PLAYER".equals(role)) {
            return new ResponseWrapper<>(200, "Fetched player's transactions", queryService.getByPlayer(userId, pageable));
        }

        return new ResponseWrapper<>(false, 400, null);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'AGENT', 'PLAYER')")
    public ResponseWrapper<Page<TransactionLogDto>> getMyTransactionLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TransactionLogDto> logs = queryService.getMyTransactionLogsSorted(page, size);
        return new ResponseWrapper<>(200, "Fetched transactions", logs);
    }
}