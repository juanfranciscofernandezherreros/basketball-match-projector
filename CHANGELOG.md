# Changelog

## 1.0.0 - 2026-09-24
- Crea el projector PostgreSQL -> MongoDB.
- Agrega por `match_id` datos de `fixtures`, `results`, `match_summary`, `stats_player`, `team_stats` y `point_by_point_event`.
- Publica el read model principal en la colección MongoDB `matches`.
- Publica el point-by-point en `match_point_by_point`, separado por cuarto para evitar documentos excesivamente grandes.
- Tolera tablas de negocio todavía no desplegadas y permite reconciliación periódica completa.
