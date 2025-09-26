package com.arenabast.api.dto;

import com.arenabast.api.enums.RoleTypes;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OwedPartyDto {
    Long userId;
    RoleTypes role;
    String name;
    Double amount;
}