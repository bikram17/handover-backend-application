package com.arenabast.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OddsSportDto {
    private String key;
    private String group;
    private String title;
    private String description;
    private boolean active;
    @JsonProperty("has_outrights")
    private boolean hasOutrights;
}
