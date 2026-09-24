package com.fernandez.basketball.projector.document;

import com.fernandez.basketball.projector.model.MatchViews;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Document("matches")
public record MatchDocument(
        @Id String id,
        String matchId,
        MatchViews.Fixture fixture,
        MatchViews.Result result,
        MatchViews.Summary summary,
        List<MatchViews.TeamPlayers> players,
        List<MatchViews.TeamStat> teamStats,
        int pointByPointEvents,
        Set<String> availableSections,
        Instant projectedAt
) {}
