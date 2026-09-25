# basketball-match-projector

![version](https://img.shields.io/badge/version-1.0.1-blue)
Microservicio Spring Boot que construye el **read model** de partidos para la futura web.

```text
PostgreSQL (source of truth)
        |
        | match_id
        v
basketball-match-projector
        |
        +----> MongoDB matches
        |
        +----> MongoDB match_point_by_point
```

## Tablas PostgreSQL leídas

El projector agrega exclusivamente información de negocio del partido:

| Tabla | Uso |
|---|---|
| `fixtures` | país, competición, fecha/hora y equipos |
| `results` | marcador final y parciales |
| `match_summary` | summary enriquecido e imágenes |
| `stats_player` | estadísticas por jugador |
| `team_stats` | estadísticas de equipo Overall/Q1/Q2/Q3/Q4 |
| `point_by_point_event` | secuencia de eventos del partido |

No proyecta tablas técnicas como estados de importación o eventos procesados.

Si alguna tabla todavía no existe en el esquema PostgreSQL, el ciclo continúa con las tablas disponibles.

## Colección `matches`

Un documento por `matchId`:

```json
{
  "_id": "0fAJZWz1",
  "matchId": "0fAJZWz1",
  "fixture": {},
  "result": {},
  "summary": {},
  "players": [
    {
      "team": "Bayern",
      "players": []
    }
  ],
  "teamStats": [],
  "pointByPointEvents": 180,
  "availableSections": [
    "fixture",
    "result",
    "summary",
    "players",
    "teamStats",
    "pointByPoint"
  ],
  "projectedAt": "2026-09-24T12:00:00Z"
}
```

## Colección `match_point_by_point`

El PBP se separa del documento principal. Hay un documento por partido y cuarto:

```json
{
  "_id": "0fAJZWz1:Q1",
  "matchId": "0fAJZWz1",
  "quarter": "Q1",
  "events": [],
  "projectedAt": "2026-09-24T12:00:00Z"
}
```

Esto mantiene ligera la carga inicial de una página de partido y permite que la API solicite el point-by-point solo cuando el usuario abre esa pestaña.

## Reconciliación

Por defecto se ejecuta un ciclo cada 60 segundos:

1. descubre todos los `match_id` existentes en las tablas soportadas;
2. consulta todas las secciones para cada partido;
3. agrupa jugadores por equipo;
4. hace upsert del documento `matches`;
5. reemplaza los documentos PBP del partido agrupándolos por cuarto.

El ciclo es idempotente: MongoDB es un read model regenerable desde PostgreSQL.

## Variables

```text
DB_URL=jdbc:postgresql://localhost:5432/basketball
DB_USER=postgres
DB_PASS=postgres
MONGODB_URI=mongodb://localhost:27017/basketball
PROJECTOR_FIXED_DELAY_MS=60000
PROJECTOR_INITIAL_DELAY_MS=5000
```

## Tests

```bash
mvn -B test
```

## Infraestructura local

```bash
docker compose up -d
```

La responsabilidad del servicio termina en MongoDB. La futura `basketball-match-api` deberá leer estas colecciones y exponerlas al frontend.
