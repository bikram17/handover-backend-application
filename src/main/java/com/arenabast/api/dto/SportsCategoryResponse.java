package com.arenabast.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SportsCategoryResponse {
    @JsonProperty("sports_categories")
    private List<SportCategoryDto> sportCategories;
}
