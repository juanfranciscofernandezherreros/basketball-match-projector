# Repository agent rules

## Mandatory pre-flight

The **first repository read operation in every task or session** MUST be reading this `AGENTS.md` from the default branch. If this file references other rule files, read them before any write.

Reading these rules in a previous chat, task, or session does not count. Do not rely on remembered instructions.

**No repository write is allowed before this pre-flight is complete.**

## Autonomous execution

After the pre-flight, continue autonomously. Choose a descriptive branch, determine the appropriate SemVer level from the real impact, and document both in the Pull Request. Do not stop for branch, SemVer, commit, push, test, PR, merge, or cleanup confirmation unless the user explicitly asks to participate.

## No direct writes to `main`

**Never modify, commit, or push directly to `main`.**

This applies to code, documentation, configuration, workflows, dependencies, versioning, badges, hotfixes, reverts, and every other repository file.

Every change must follow this workflow:

1. Read `AGENTS.md` and any referenced rules.
2. Start from updated `main`.
3. Create a dedicated branch before modifying any file.
4. Apply the appropriate SemVer change.
5. Make all changes only on that branch.
6. Update `CHANGELOG.md`, `README.md`, and version metadata when applicable.
7. Run the applicable checks, at minimum `mvn -B test`.
8. Open or update a Pull Request to `main`.
9. Check required/applicable CI on the current PR SHA.
10. Fix failures in the same branch/PR and re-run checks.
11. Merge only when required/applicable checks are green and no GitHub protection blocks the merge.
12. Delete only the PR source branch after merge and verify that it no longer exists.

Work is not complete until merge and branch cleanup are complete.

## Project baseline

- Java 21.
- PostgreSQL is the source of truth.
- MongoDB stores the projected read model.
- Preserve established architecture and datasource choices unless the user explicitly requests a change.

## Tests

Run at minimum `mvn -B test`. Functional JUnit tests must not assert release/version numbers from `pom.xml`, `README.md`, badges, or `CHANGELOG.md`; release metadata validation belongs in repository CI/policy checks.

## Operational safety

Every merge decision must use the current PR SHA. If a user instruction conflicts with these rules, stop only the incompatible operation; never improvise a direct write to `main`.
