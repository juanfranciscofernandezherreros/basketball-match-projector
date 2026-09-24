package com.fernandez.basketball.projector.service;

import com.fernandez.basketball.projector.document.MatchDocument;
import com.fernandez.basketball.projector.document.PointByPointQuarterDocument;
import com.fernandez.basketball.projector.model.MatchViews;
import com.fernandez.basketball.projector.source.PostgresMatchReader;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchProjectionServiceTest {

    @Test
    void projectsAllBusinessSectionsAndSplitsPointByPointByQuarter() {
        PostgresMatchReader reader = mock(PostgresMatchReader.class);
        MongoTemplate mongo = mock(MongoTemplate.class);

        when(reader.fixture("m1")).thenReturn(Optional.of(
                new MatchViews.Fixture("Germany", "BBL", "2026-09-20", "Bamberg", "Bayern")
        ));
        when(reader.result("m1")).thenReturn(Optional.of(
                new MatchViews.Result("event-1", "2026-09-20", "Bamberg", "Bayern",
                        72, 108, List.of(18, 17, 20, 17), List.of(27, 25, 29, 27),
                        "Germany", "BBL")
        ));
        when(reader.summary("m1")).thenReturn(Optional.empty());
        when(reader.players("m1")).thenReturn(List.of(
                player("Player A", "Bamberg", 15),
                player("Player B", "Bayern", 22)
        ));
        when(reader.teamStats("m1")).thenReturn(List.of(
                new MatchViews.TeamStat("Overall", "Scoring", "Field Goal Attempts",
                        "Bamberg", "61", "Bayern", "66", "https://example")
        ));
        when(reader.pointByPoint("m1")).thenReturn(List.of(
                event("Q1", 1, 0, 2),
                event("Q1", 2, 2, 2),
                event("Q2", 1, 18, 29)
        ));

        MatchDocument projected = new MatchProjectionService(reader, mongo).project("m1");

        assertThat(projected.matchId()).isEqualTo("m1");
        assertThat(projected.players()).hasSize(2);
        assertThat(projected.teamStats()).hasSize(1);
        assertThat(projected.pointByPointEvents()).isEqualTo(3);
        assertThat(projected.availableSections())
                .containsExactlyInAnyOrder("fixture", "result", "players", "teamStats", "pointByPoint");

        verify(mongo).save(any(MatchDocument.class));
        verify(mongo, times(2)).save(any(PointByPointQuarterDocument.class));
    }

    private MatchViews.Player player(String name, String team, Integer pts) {
        return new MatchViews.Player(name, team, pts, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null);
    }

    private MatchViews.PointByPointEvent event(String quarter, int sequence, int home, int away) {
        return new MatchViews.PointByPointEvent(
                "score", quarter, sequence, home, away, null, null,
                null, null, null, home > away, away > home
        );
    }
}
