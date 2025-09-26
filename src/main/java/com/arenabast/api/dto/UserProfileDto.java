package com.arenabast.api.dto;

import com.arenabast.api.enums.RoleTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String name;
    private String userName;
    private String email;
    private RoleTypes role;
    private boolean active;
    private Date createdAt;
}
