package com.fernandez.basketball.projector.service;

import com.fernandez.basketball.projector.document.MatchDocument;
import com.fernandez.basketball.projector.document.PointByPointQuarterDocument;
import com.fernandez.basketball.projector.model.MatchViews;
import com.fernandez.basketball.projector.source.PostgresMatchReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class MatchProjectionService {

    private static final Logger log = LoggerFactory.getLogger(MatchProjectionService.class);

    private final PostgresMatchReader reader;
    private final MongoTemplate mongo;

    public MatchProjectionService(PostgresMatchReader reader, MongoTemplate mongo) {
        this.reader = reader;
        this.mongo = mongo;
    }

    public int projectAll() {
        int projected = 0;
        for (String matchId : reader.findAllMatchIds()) {
            try {
                project(matchId);
                projected++;
            } catch (RuntimeException ex) {
                log.error("Could not project match {}", matchId, ex);
            }
        }
        return projected;
    }

    public MatchDocument project(String matchId) {
        Objects.requireNonNull(matchId, "matchId");
        Instant now = Instant.now();

        var fixture = reader.fixture(matchId);
        var result = reader.result(matchId);
        var summary = reader.summary(matchId);
        var players = reader.players(matchId);
        var teamStats = reader.teamStats(matchId);
        var pointByPoint = reader.pointByPoint(matchId);

        List<MatchViews.TeamPlayers> playersByTeam = groupPlayers(players);
        Set<String> availableSections = new LinkedHashSet<>();
        fixture.ifPresent(v -> availableSections.add("fixture"));
        result.ifPresent(v -> availableSections.add("result"));
        summary.ifPresent(v -> availableSections.add("summary"));
        if (!players.isEmpty()) availableSections.add("players");
        if (!teamStats.isEmpty()) availableSections.add("teamStats");
        if (!pointByPoint.isEmpty()) availableSections.add("pointByPoint");

        MatchDocument match = new MatchDocument(
                matchId,
                matchId,
                fixture.orElse(null),
                result.orElse(null),
                summary.orElse(null),
                playersByTeam,
                teamStats,
                pointByPoint.size(),
                Collections.unmodifiableSet(availableSections),
                now
        );

        mongo.save(match);
        replacePointByPoint(matchId, pointByPoint, now);

        log.info("Projected match {} with sections {}", matchId, availableSections);
        return match;
    }

    private List<MatchViews.TeamPlayers> groupPlayers(List<MatchViews.Player> players) {
        Map<String, List<MatchViews.Player>> grouped = new LinkedHashMap<>();
        for (MatchViews.Player player : players) {
            String team = player.team() == null || player.team().isBlank() ? "UNKNOWN" : player.team();
            grouped.computeIfAbsent(team, ignored -> new ArrayList<>()).add(player);
        }
        return grouped.entrySet().stream()
                .map(e -> new MatchViews.TeamPlayers(e.getKey(), List.copyOf(e.getValue())))
                .toList();
    }

    private void replacePointByPoint(
            String matchId,
            List<MatchViews.PointByPointEvent> events,
            Instant projectedAt
    ) {
        mongo.remove(
                Query.query(Criteria.where("matchId").is(matchId)),
                PointByPointQuarterDocument.class
        );

        Map<String, List<MatchViews.PointByPointEvent>> byQuarter = new LinkedHashMap<>();
        for (MatchViews.PointByPointEvent event : events) {
            String quarter = event.quarter() == null || event.quarter().isBlank() ? "UNKNOWN" : event.quarter();
            byQuarter.computeIfAbsent(quarter, ignored -> new ArrayList<>()).add(event);
        }

        byQuarter.forEach((quarter, quarterEvents) ->
                mongo.save(new PointByPointQuarterDocument(
                        matchId + ":" + quarter,
                        matchId,
                        quarter,
                        List.copyOf(quarterEvents),
                        projectedAt
                ))
        );
    }
}
