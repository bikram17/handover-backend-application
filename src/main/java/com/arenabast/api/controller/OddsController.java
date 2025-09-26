package com.arenabast.api.controller;

import com.arenabast.api.dto.*;
import com.arenabast.api.service.OddsApiService;
import com.arenabast.api.service.SportsCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/odds")
@RequiredArgsConstructor
public class OddsController extends ApiRestHandler {
    private final OddsApiService oddsApiService;
    private final SportsCategoryService sportsCategoryService;

    @GetMapping("/sports")
    public ResponseWrapper<List<SportDto>> getSports() {
        return new ResponseWrapper<>(true, 200, oddsApiService.getAllSports());
    }

    @GetMapping("/events/{sportKey}")
    public ResponseWrapper<List<EventDto>> getEvents(@PathVariable String sportKey) {
        return new ResponseWrapper<>(true, 200, "odds data", oddsApiService.getUpcomingEvents(sportKey));
    }

    // ✅ New endpoint for scores
    @GetMapping("/scores/{sportKey}")
    public ResponseWrapper<List<GameDto>> getScores(
            @PathVariable String sportKey,
            @RequestParam(required = false) Integer daysFrom,
            @RequestParam(required = false, defaultValue = "iso") String dateFormat
    ) {
        List<GameDto> scores = oddsApiService.getScores(sportKey, daysFrom, dateFormat);
        return new ResponseWrapper<>(200, "Fetched scores", scores);
    }

    @GetMapping("/sports/categories")
    public ResponseWrapper<SportsCategoryResponse> getSportsCategories(
            @RequestParam(defaultValue = "false") boolean include_leagues,
            @RequestParam(defaultValue = "true") boolean active_only,
            @RequestParam(required = false) Boolean featured
    ) {
        SportsCategoryResponse response =
                sportsCategoryService.getSportsCategories(include_leagues, active_only, featured);
        return new ResponseWrapper<>(200, "Fetched sports categories", response);
    }

    @GetMapping("/odds")
    public ResponseWrapper<List<EventDto>> getOdds(
            @RequestParam String sport,
            @RequestParam(defaultValue = "eu") String regions,
            @RequestParam(defaultValue = "h2h") String markets,
            @RequestParam(required = false) String dateFormat,
            @RequestParam(required = false) String oddsFormat
    ) {
        List<EventDto> events = oddsApiService.getOdds(sport, regions, markets, dateFormat, oddsFormat);
        return new ResponseWrapper<>(200, "odds", events);
    }

    @GetMapping("/events")
    public ResponseWrapper<List<EventDto>> getEvents(
            @RequestParam String sport,
            @RequestParam(required = false) String dateFormat,
            @RequestParam(required = false) String eventIds,
            @RequestParam(required = false) String commenceTimeFrom,
            @RequestParam(required = false) String commenceTimeTo
    ) {
        List<EventDto> events = oddsApiService.getEvents(sport, dateFormat, eventIds, commenceTimeFrom, commenceTimeTo);
        return new ResponseWrapper<>(200, "events", events);
    }

    @GetMapping("/event-odds")
    public ResponseWrapper<EventOddsDto> getEventOdds(
            @RequestParam String sport,
            @RequestParam String eventId,
            @RequestParam String regions,
            @RequestParam(required = false) String markets,
            @RequestParam(required = false) String dateFormat,
            @RequestParam(required = false) String oddsFormat
    ) {
        EventOddsDto odds = oddsApiService.getEventOdds(sport, eventId, regions, markets, dateFormat, oddsFormat);
        return new ResponseWrapper<>(200, "event-odds", odds);
    }

    @GetMapping("/event-markets")
    public ResponseWrapper<EventMarketsDto> getEventMarkets(
            @RequestParam String sport,
            @RequestParam String eventId,
            @RequestParam String regions,
            @RequestParam(required = false) String bookmakers,
            @RequestParam(required = false) String dateFormat
    ) {
        EventMarketsDto markets = oddsApiService.getEventMarkets(sport, eventId, regions, bookmakers, dateFormat);
        return new ResponseWrapper<>(200, "event-markets", markets);
    }


    @GetMapping("/participants")
    public ResponseWrapper<List<ParticipantDto>> getParticipants(@RequestParam String sport) {
        return new ResponseWrapper<>(200, "participants", oddsApiService.getParticipants(sport));
    }

    @GetMapping("/historical-odds")
    public ResponseWrapper<HistoricalOddsResponse> getHistoricalOdds(
            @RequestParam String sport,
            @RequestParam String regions,
            @RequestParam String markets,
            @RequestParam String date,
            @RequestParam(defaultValue = "american") String oddsFormat) {

        return new ResponseWrapper<>(200, "historical-odds",
                oddsApiService.getHistoricalOdds(sport, regions, markets, date, oddsFormat)
        );
    }

    @GetMapping("/historical-events")
    public ResponseWrapper<HistoricalEventsResponse> getHistoricalEvents(
            @RequestParam String sport,
            @RequestParam String date,
            @RequestParam(required = false) String dateFormat,
            @RequestParam(required = false) String commenceTimeFrom,
            @RequestParam(required = false) String commenceTimeTo,
            @RequestParam(required = false) String eventIds) {

        return new ResponseWrapper<>(200, "historical-odds",
                oddsApiService.getHistoricalEvents(sport, date, dateFormat, commenceTimeFrom, commenceTimeTo, eventIds)
        );
    }

    @GetMapping("/historical-event-odds/{sport}/{eventId}")
    public ResponseWrapper<HistoricalEventOddsResponse> getHistoricalEventOdds(
            @PathVariable String sport,
            @PathVariable String eventId,
            @RequestParam String date,
            @RequestParam(required = false) String regions,
            @RequestParam(required = false) String markets,
            @RequestParam(required = false) String dateFormat,
            @RequestParam(required = false) String oddsFormat) {

        return new ResponseWrapper<>(200, "historical-odds",
                oddsApiService.getHistoricalEventOdds(sport, eventId, date, regions, markets, dateFormat, oddsFormat)
        );
    }
}
