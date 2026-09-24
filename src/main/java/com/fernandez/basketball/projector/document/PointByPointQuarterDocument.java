package com.fernandez.basketball.projector.document;

import com.fernandez.basketball.projector.model.MatchViews;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("match_point_by_point")
public record PointByPointQuarterDocument(
        @Id String id,
        @Indexed String matchId,
        String quarter,
        List<MatchViews.PointByPointEvent> events,
        Instant projectedAt
) {}
