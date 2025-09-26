package com.arenabast.api.service;

import com.arenabast.api.dto.LeagueDto;
import com.arenabast.api.dto.OddsSportDto;
import com.arenabast.api.dto.SportCategoryDto;
import com.arenabast.api.dto.SportsCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class SportsCategoryService {

    private static final String BASE_URL = "https://api.the-odds-api.com/v4";
    private final RestTemplate restTemplate = new RestTemplate();
    private final OddsApiService oddsApiService;
    @Value("${odds.api.key}")
    private String apiKey;

    public SportsCategoryResponse getSportsCategories(
            Boolean includeLeagues, Boolean activeOnly, Boolean featured
    ) {
        // Step 1: Fetch sports from Odds API
        String url = String.format("%s/sports/?apiKey=%s", BASE_URL, apiKey);
        OddsSportDto[] oddsSports = restTemplate.getForObject(url, OddsSportDto[].class);

        if (oddsSports == null) {
            return new SportsCategoryResponse(Collections.emptyList());
        }

        // Step 2: Transform
        List<SportCategoryDto> categories = new ArrayList<>();
        AtomicLong idCounter = new AtomicLong(1);

        for (OddsSportDto oddsSport : oddsSports) {
            // Apply "active_only"
            if (Boolean.TRUE.equals(activeOnly) && !oddsSport.isActive()) {
                continue;
            }

            // Example featured logic: mark NBA + EPL as featured
            boolean isFeatured = "basketball_nba".equals(oddsSport.getKey())
                    || "soccer_epl".equals(oddsSport.getKey());

            if (Boolean.TRUE.equals(featured) && !isFeatured) {
                continue;
            }

            SportCategoryDto dto = new SportCategoryDto();
            dto.setSport_id(idCounter.getAndIncrement());
            dto.setSport_name(oddsSport.getTitle());
            dto.setSport_code(oddsSport.getKey());
            dto.setIcon_url("/icons/" + oddsSport.getKey() + ".png");
            dto.setDisplay_order(dto.getSport_id().intValue());
            dto.setIs_featured(isFeatured);

            // Real active event count from Odds API events
            int activeEventsCount = oddsApiService.getUpcomingEvents(oddsSport.getKey()).size();
            dto.setActive_events_count(activeEventsCount);

            // include leagues if requested
            if (Boolean.TRUE.equals(includeLeagues)) {
                LeagueDto leagueDto = new LeagueDto(
                        dto.getSport_id(),
                        oddsSport.getTitle() + " League",
                        "Unknown",
                        "",
                        "",
                        activeEventsCount,
                        isFeatured
                );
                dto.setLeagues(List.of(leagueDto));
            } else {
                dto.setLeagues(null);
            }

            categories.add(dto);
        }

        return new SportsCategoryResponse(categories);
    }
}