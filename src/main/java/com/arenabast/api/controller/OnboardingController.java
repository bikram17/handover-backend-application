package com.arenabast.api.controller;

import com.arenabast.api.dto.ResponseWrapper;
import com.arenabast.api.dto.UserAddRequestDto;
import com.arenabast.api.enums.TransactionType;
import com.arenabast.api.service.OnboardingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/v1/onboard")
@Slf4j
public class OnboardingController extends ApiRestHandler {


    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN, ADMIN, AGENT')")
    @GetMapping("get-transaction-constants")
    public ResponseWrapper<List<TransactionType>> getTransactionConstants() {
        List<TransactionType> transactionTypes = Arrays.asList(TransactionType.values());
        return new ResponseWrapper<>(200, "OK", transactionTypes);
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'AGENT')")
    @PostMapping("/add/user")
    public ResponseWrapper<String> addUser(@RequestBody UserAddRequestDto userAddRequestDto) {
        onboardingService.addUser(userAddRequestDto);
        return new ResponseWrapper<>(200, "OK", "User added");
    }
}
