package com.arenabast.api.service;

import com.arenabast.api.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OddsApiService {

    private static final String BASE_URL = "https://api.odds-api.io/v3";
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${odds.api.key}")
    private String apiKey;

    // ✅ Fetch all sports
    public List<SportDto> getAllSports() {
        String url = BASE_URL + "/sports/?apiKey=" + apiKey;
        SportDto[] sports = restTemplate.getForObject(url, SportDto[].class);
        return Arrays.asList(sports);
    }

    // ✅ Fetch upcoming events for a sport
    public List<EventDto> getUpcomingEvents(String sportKey) {
        String url = String.format(
                "%s/sports/%s/odds/?apiKey=%s&regions=eu&markets=h2h,spreads,totals",
                BASE_URL, sportKey, apiKey
        );

        EventDto[] events = restTemplate.getForObject(url, EventDto[].class);

        return events != null ? Arrays.asList(events) : Collections.emptyList();
    }

    // ✅ New method for scores
    public List<GameDto> getScores(String sportKey, Integer daysFrom, String dateFormat) {
        String url = String.format("%s/sports/%s/scores/?apiKey=%s", BASE_URL, sportKey, apiKey);

        if (daysFrom != null) {
            url += "&daysFrom=" + daysFrom;
        }
        if (dateFormat != null) {
            url += "&dateFormat=" + dateFormat;
        }

        GameDto[] games = restTemplate.getForObject(url, GameDto[].class);
        return games != null ? Arrays.asList(games) : Collections.emptyList();
    }

    public List<EventDto> getOdds(
            String sport,
            String regions,
            String markets,
            String dateFormat,
            String oddsFormat
    ) {
        StringBuilder url = new StringBuilder(
                String.format("%s/sports/%s/odds/?apiKey=%s", BASE_URL, sport, apiKey)
        );

        url.append("&regions=").append(regions != null ? regions : "eu");
        url.append("&markets=").append(markets != null ? markets : "h2h");

        if (dateFormat != null) {
            url.append("&dateFormat=").append(dateFormat);
        }
        if (oddsFormat != null) {
            url.append("&oddsFormat=").append(oddsFormat);
        }

        EventDto[] events = restTemplate.getForObject(url.toString(), EventDto[].class);
        return events != null ? Arrays.asList(events) : Collections.emptyList();
    }

    public List<EventDto> getEvents(
            String sport,
            String dateFormat,
            String eventIds,
            String commenceTimeFrom,
            String commenceTimeTo
    ) {
        StringBuilder url = new StringBuilder(
                String.format("%s/sports/%s/events?apiKey=%s", BASE_URL, sport, apiKey)
        );

        if (dateFormat != null) {
            url.append("&dateFormat=").append(dateFormat);
        }
        if (eventIds != null) {
            url.append("&eventIds=").append(eventIds);
        }
        if (commenceTimeFrom != null) {
            url.append("&commenceTimeFrom=").append(commenceTimeFrom);
        }
        if (commenceTimeTo != null) {
            url.append("&commenceTimeTo=").append(commenceTimeTo);
        }

        EventDto[] events = restTemplate.getForObject(url.toString(), EventDto[].class);
        return events != null ? Arrays.asList(events) : Collections.emptyList();
    }

    // ✅ Fetch Odds for a single Event
    public EventOddsDto getEventOdds(
            String sport,
            String eventId,
            String regions,
            String markets,
            String dateFormat,
            String oddsFormat
    ) {
        StringBuilder url = new StringBuilder(
                String.format("%s/sports/%s/events/%s/odds?apiKey=%s", BASE_URL, sport, eventId, apiKey)
        );

        if (regions != null) url.append("&regions=").append(regions);
        if (markets != null) url.append("&markets=").append(markets);
        if (dateFormat != null) url.append("&dateFormat=").append(dateFormat);
        if (oddsFormat != null) url.append("&oddsFormat=").append(oddsFormat);

        return restTemplate.getForObject(url.toString(), EventOddsDto.class);
    }

    // ✅ Fetch available markets for a single event
    public EventMarketsDto getEventMarkets(
            String sport,
            String eventId,
            String regions,
            String bookmakers,
            String dateFormat
    ) {
        StringBuilder url = new StringBuilder(
                String.format("%s/sports/%s/events/%s/markets?apiKey=%s", BASE_URL, sport, eventId, apiKey)
        );

        if (regions != null) url.append("&regions=").append(regions);
        if (bookmakers != null) url.append("&bookmakers=").append(bookmakers);
        if (dateFormat != null) url.append("&dateFormat=").append(dateFormat);

        return restTemplate.getForObject(url.toString(), EventMarketsDto.class);
    }

    // ✅ Fetch participants (teams/players) for a sport
    public List<ParticipantDto> getParticipants(String sport) {
        String url = String.format("%s/sports/%s/participants?apiKey=%s", BASE_URL, sport, apiKey);
        ParticipantDto[] participants = restTemplate.getForObject(url, ParticipantDto[].class);
        return participants != null ? Arrays.asList(participants) : Collections.emptyList();
    }

    public HistoricalOddsResponse getHistoricalOdds(String sport,
                                                    String regions,
                                                    String markets,
                                                    String date,
                                                    String oddsFormat) {

        String url = String.format(
                "%s/historical/sports/%s/odds?apiKey=%s&regions=%s&markets=%s&date=%s&oddsFormat=%s",
                BASE_URL, sport, apiKey, regions, markets, date, oddsFormat
        );

        return restTemplate.getForObject(url, HistoricalOddsResponse.class);
    }

    // ✅ Get historical events snapshot
    public HistoricalEventsResponse getHistoricalEvents(String sport,
                                                        String date,
                                                        String dateFormat,
                                                        String commenceTimeFrom,
                                                        String commenceTimeTo,
                                                        String eventIds) {

        StringBuilder url = new StringBuilder(String.format(
                "%s/historical/sports/%s/events?apiKey=%s&date=%s",
                BASE_URL, sport, apiKey, date
        ));

        if (dateFormat != null) url.append("&dateFormat=").append(dateFormat);
        if (commenceTimeFrom != null) url.append("&commenceTimeFrom=").append(commenceTimeFrom);
        if (commenceTimeTo != null) url.append("&commenceTimeTo=").append(commenceTimeTo);
        if (eventIds != null) url.append("&eventIds=").append(eventIds);

        return restTemplate.getForObject(url.toString(), HistoricalEventsResponse.class);
    }

    // ✅ Get historical event odds snapshot
    public HistoricalEventOddsResponse getHistoricalEventOdds(String sport,
                                                              String eventId,
                                                              String date,
                                                              String regions,
                                                              String markets,
                                                              String dateFormat,
                                                              String oddsFormat) {

        StringBuilder url = new StringBuilder(String.format(
                "%s/historical/sports/%s/events/%s/odds?apiKey=%s&date=%s",
                BASE_URL, sport, eventId, apiKey, date
        ));

        if (regions != null) url.append("&regions=").append(regions);
        if (markets != null) url.append("&markets=").append(markets);
        if (dateFormat != null) url.append("&dateFormat=").append(dateFormat);
        if (oddsFormat != null) url.append("&oddsFormat=").append(oddsFormat);

        return restTemplate.getForObject(url.toString(), HistoricalEventOddsResponse.class);
    }
}
