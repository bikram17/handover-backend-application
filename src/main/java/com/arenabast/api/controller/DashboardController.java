package com.arenabast.api.controller;

import com.arenabast.api.dto.AdminDashboardSummaryDto;
import com.arenabast.api.dto.ResponseWrapper;
import com.arenabast.api.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {


    private final AdminService adminService;

    public DashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/admin-summary")
    public ResponseWrapper<AdminDashboardSummaryDto> getAdminDashboardSummary() {
        AdminDashboardSummaryDto summary = adminService.getAdminDashboardSummary();
        return new ResponseWrapper<>(true, 200, summary);
    }

}
