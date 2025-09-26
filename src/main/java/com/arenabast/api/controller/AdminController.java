package com.arenabast.api.controller;

import com.arenabast.api.dto.*;
import com.arenabast.api.dto.login.LoginRequestDto;
import com.arenabast.api.dto.login.LoginResponseDto;
import com.arenabast.api.service.AdminService;
import com.arenabast.api.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/v1/auth")
@Slf4j
public class AdminController extends ApiRestHandler {


    private final AuthService authService;
    private final AdminService adminService;

    public AdminController(AuthService authService, AdminService adminService) {
        this.authService = authService;
        this.adminService = adminService;
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseWrapper<AdminResponseDto>> addAdmin(@RequestBody AdminRequestDto request) {
        AdminResponseDto createdAdmin = adminService.createAdmin(request);
        return ResponseEntity.ok(new ResponseWrapper<>(200, "Admin created", createdAdmin));
    }


    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<ResponseWrapper<PaginatedResponse<AdminResponseDto>>> listAdmins(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String name, // username filter
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaginatedResponse<AdminResponseDto> result = adminService.getAllAdmins(active, name, startDate, endDate, page, size);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, result));
    }


    @PostMapping("/login")
    public ResponseWrapper<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        log.info("Login request: {}", request);
        return new ResponseWrapper<>(true, 200, authService.login(request));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/admin/edit/{adminId}")
    public ResponseWrapper<String> editAdmin(
            @PathVariable Long adminId,
            @RequestBody AdminEditRequestDto requestDto) {
        adminService.updateAdmin(adminId, requestDto);
        return new ResponseWrapper<>(true, 200, "Admin updated successfully");
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/admin/status/{adminId}")
    public ResponseWrapper<String> toggleAdminStatus(
            @PathVariable Long adminId,
            @RequestParam boolean active) {
        adminService.changeAdminStatus(adminId, active);
        return new ResponseWrapper<>(true, 200, "Admin status updated");
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseWrapper<AdminResponseDto> getAdminById(@PathVariable Long id) {
        return new ResponseWrapper<>(true, 200, adminService.getAdminById(id));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'AGENT')")
    @PutMapping("/profile")
    public ResponseWrapper<String> updateOwnProfile(@RequestBody UpdateProfileRequest request) {
        adminService.updateOwnProfile(request);
        return new ResponseWrapper<>(200, "Profile updated successfully", null);
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'AGENT')")
    @GetMapping("/profile")
    public ResponseWrapper<UserProfileDto> getOwnProfile() {
        UserProfileDto profile = adminService.getOwnProfile();
        return new ResponseWrapper<>(200, "Profile fetched successfully", profile);
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/admin/export")
    public ResponseEntity<byte[]> exportAdminsAsCsv(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) throws IOException {

        // Get all admins without pagination for export
        List<AdminResponseDto> admins = adminService.getAllAdminsForExport(active, name, startDate, endDate);

        // Convert to CSV
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID,Name,Email,Active,Role,Onboarded Date,Wallet Balance,Assigned AgentCount\n");

        for (AdminResponseDto admin : admins) {
            csvBuilder.append(admin.getId()).append(",");
            csvBuilder.append(escapeCsv(admin.getName())).append(",");
            csvBuilder.append(escapeCsv(admin.getEmail())).append(",");
            csvBuilder.append(admin.isActive()).append(",");
            csvBuilder.append(admin.getRoleType()).append(",");
            csvBuilder.append(admin.getOnboardedDate()).append(",");
            csvBuilder.append(admin.getWalletBalance()).append(",");
            csvBuilder.append(admin.getAssignedAgentCount()).append("\n");
        }

        byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("admins_export.csv")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }


}
