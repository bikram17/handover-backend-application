package com.arenabast.api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController extends ApiRestHandler {

    @GetMapping("/hello/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String helloAdmin() {
        return "Hello world";
    }

    @GetMapping("/hello/agent")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public String helloAgent() {
        return "Hello world";
    }

    @GetMapping("/hello/shared")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public String helloShared() {
        return "Hello world for admin and super admin";
    }
}