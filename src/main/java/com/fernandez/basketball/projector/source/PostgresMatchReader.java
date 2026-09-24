package com.fernandez.basketball.projector.source;

import com.fernandez.basketball.projector.model.MatchViews;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PostgresMatchReader {

    private static final List<String> MATCH_TABLES = List.of(
            "fixtures",
            "results",
            "match_summary",
            "stats_player",
            "team_stats",
            "point_by_point_event"
    );

    private final JdbcTemplate jdbc;

    public PostgresMatchReader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Set<String> findAllMatchIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (String table : MATCH_TABLES) {
            if (!tableExists(table)) {
                continue;
            }
            jdbc.query("SELECT DISTINCT match_id FROM " + table + " WHERE match_id IS NOT NULL",
                    rs -> {
                        String matchId = rs.getString(1);
                        if (matchId != null && !matchId.isBlank()) {
                            ids.add(matchId);
                        }
                    });
        }
        return ids;
    }

    public Optional<MatchViews.Fixture> fixture(String matchId) {
        if (!tableExists("fixtures")) return Optional.empty();
        var rows = jdbc.queryForList("""
                SELECT country, competition, event_time, home_team, away_team
                FROM fixtures
                WHERE match_id = ?
                ORDER BY country, competition
                LIMIT 1
                """, matchId);
        if (rows.isEmpty()) return Optional.empty();
        Map<String, Object> r = rows.getFirst();
        return Optional.of(new MatchViews.Fixture(
                string(r, "country"),
                string(r, "competition"),
                string(r, "event_time"),
                string(r, "home_team"),
                string(r, "away_team")
        ));
    }

    public Optional<MatchViews.Result> result(String matchId) {
        if (!tableExists("results")) return Optional.empty();
        var rows = jdbc.queryForList("""
                SELECT source_event_id, event_time, home_team, away_team,
                       home_score, away_score,
                       home_score1, home_score2, home_score3, home_score4, home_score5,
                       away_score1, away_score2, away_score3, away_score4, away_score5,
                       country, competition
                FROM results
                WHERE match_id = ?
                """, matchId);
        if (rows.isEmpty()) return Optional.empty();
        Map<String, Object> r = rows.getFirst();
        return Optional.of(new MatchViews.Result(
                string(r, "source_event_id"),
                string(r, "event_time"),
                string(r, "home_team"),
                string(r, "away_team"),
                integer(r, "home_score"),
                integer(r, "away_score"),
                Arrays.asList(
                        integer(r, "home_score1"),
                        integer(r, "home_score2"),
                        integer(r, "home_score3"),
                        integer(r, "home_score4"),
                        integer(r, "home_score5")
                ),
                Arrays.asList(
                        integer(r, "away_score1"),
                        integer(r, "away_score2"),
                        integer(r, "away_score3"),
                        integer(r, "away_score4"),
                        integer(r, "away_score5")
                ),
                string(r, "country"),
                string(r, "competition")
        ));
    }

    public Optional<MatchViews.Summary> summary(String matchId) {
        if (!tableExists("match_summary")) return Optional.empty();
        var rows = jdbc.queryForList("""
                SELECT date, home_name, home_image, away_name, away_image,
                       result_home, result_away,
                       total_local, first_local, second_local, third_local, fourth_local, extra_local,
                       total_away, first_away, second_away, third_away, fourth_away, extra_away
                FROM match_summary
                WHERE match_id = ?
                """, matchId);
        if (rows.isEmpty()) return Optional.empty();
        Map<String, Object> r = rows.getFirst();
        return Optional.of(new MatchViews.Summary(
                string(r, "date"),
                string(r, "home_name"),
                string(r, "home_image"),
                string(r, "away_name"),
                string(r, "away_image"),
                string(r, "result_home"),
                string(r, "result_away"),
                string(r, "total_local"),
                string(r, "first_local"),
                string(r, "second_local"),
                string(r, "third_local"),
                string(r, "fourth_local"),
                string(r, "extra_local"),
                string(r, "total_away"),
                string(r, "first_away"),
                string(r, "second_away"),
                string(r, "third_away"),
                string(r, "fourth_away"),
                string(r, "extra_away")
        ));
    }

    public List<MatchViews.Player> players(String matchId) {
        if (!tableExists("stats_player")) return List.of();
        return jdbc.queryForList("""
                SELECT name, team, pts, reb, ast, min, fgm, fga, twopm, twopa,
                       threepm, threepa, ftm, fta, plus_minus, "or", dr, pf, st, "to", bs, ba, tfs
                FROM stats_player
                WHERE match_id = ?
                ORDER BY team, name
                """, matchId).stream().map(r -> new MatchViews.Player(
                string(r, "name"),
                string(r, "team"),
                integer(r, "pts"),
                integer(r, "reb"),
                integer(r, "ast"),
                string(r, "min"),
                integer(r, "fgm"),
                integer(r, "fga"),
                integer(r, "twopm"),
                integer(r, "twopa"),
                integer(r, "threepm"),
                integer(r, "threepa"),
                integer(r, "ftm"),
                integer(r, "fta"),
                integer(r, "plus_minus"),
                integer(r, "or"),
                integer(r, "dr"),
                integer(r, "pf"),
                integer(r, "st"),
                integer(r, "to"),
                integer(r, "bs"),
                integer(r, "ba"),
                integer(r, "tfs")
        )).toList();
    }

    public List<MatchViews.TeamStat> teamStats(String matchId) {
        if (!tableExists("team_stats")) return List.of();
        return jdbc.queryForList("""
                SELECT period, category, metric, home_team, home_value,
                       away_team, away_value, source_url
                FROM team_stats
                WHERE match_id = ?
                ORDER BY period, category, metric
                """, matchId).stream().map(r -> new MatchViews.TeamStat(
                string(r, "period"),
                string(r, "category"),
                string(r, "metric"),
                string(r, "home_team"),
                string(r, "home_value"),
                string(r, "away_team"),
                string(r, "away_value"),
                string(r, "source_url")
        )).toList();
    }

    public List<MatchViews.PointByPointEvent> pointByPoint(String matchId) {
        if (!tableExists("point_by_point_event")) return List.of();
        return jdbc.queryForList("""
                SELECT record_type, quarter, sequence, home_score, away_score,
                       home_points_added, away_points_added, leader_side, advantage,
                       advantage_direction, home_is_winning, away_is_winning
                FROM point_by_point_event
                WHERE match_id = ?
                ORDER BY quarter, sequence
                """, matchId).stream().map(r -> new MatchViews.PointByPointEvent(
                string(r, "record_type"),
                string(r, "quarter"),
                integer(r, "sequence"),
                integer(r, "home_score"),
                integer(r, "away_score"),
                integer(r, "home_points_added"),
                integer(r, "away_points_added"),
                string(r, "leader_side"),
                string(r, "advantage"),
                string(r, "advantage_direction"),
                bool(r, "home_is_winning"),
                bool(r, "away_is_winning")
        )).toList();
    }

    boolean tableExists(String table) {
        Boolean exists = jdbc.queryForObject("""
                SELECT EXISTS (
                  SELECT 1
                  FROM information_schema.tables
                  WHERE table_schema = current_schema()
                    AND table_name = ?
                )
                """, Boolean.class, table);
        return Boolean.TRUE.equals(exists);
    }

    private String string(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : value.toString();
    }

    private Integer integer(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        String text = value.toString();
        return text.isBlank() ? null : Integer.valueOf(text);
    }

    private Boolean bool(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        return Boolean.valueOf(value.toString());
    }
}
