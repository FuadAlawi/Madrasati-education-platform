# Madrasati Reliability Strategy Report

## 1. Testing Strategy
- Unit tests: Services (`AssignmentService`, `IdentityService`, `CircuitBreaker`).
- Integration tests: REST endpoints `/api/auth/login`, `/api/assignments/submit`, `/api/exam/submit`.
- Load tests: Locust simulates 10k concurrent students; JMeter exam submission stress.
- Chaos tests: Induce failures in `ExamController` and `IdentityService` by increasing failure rates to validate resilience.
- Security tests: Input validation, authentication flows, rate limiting (future work), dependency scans.

## 2. Reliability Metrics
- MTBF = Total Uptime / Failures. Target: ≥ 30 days.
- MTTR = Total Downtime / Failures. Target: ≤ 15 minutes (automated rollback + runbooks).
- Availability = MTBF / (MTBF + MTTR). Target: ≥ 99.95% (exam windows ≥ 99.99%).
- POFOD = Failures / Demands. Target: ≤ 10^-4 for critical exam flows.
- ROCOF = Failures / Time interval. Target: ≤ 1/month in production.

Formulas and example calculations are included in the README and can be automated via monitoring.

## 3. Validation Plan
- Simulations: traffic via Locust and JMeter; validate latency < 300ms p95 for login and assignment submission; error rate < 0.5%.
- Monitoring: Collect metrics via Spring Actuator + external APM; dashboards for SLOs (availability, latency, error rate).
- Disaster Recovery Drills: quarterly region failover simulation; backup restore tests; RTO ≤ 30 min, RPO ≤ 5 min.
- UAT: student/teacher scenarios with accessibility and multilingual checks.

## 4. Humanization Checklist
- Equity: no discrimination by device or bandwidth; provide low-bandwidth mode and offline-friendly submissions.
- Transparency: clear status messages and maintenance windows; visible SLAs during exam periods.
- Cultural Respect: localized UI/RTL support; culturally appropriate language and examples.
- Oversight: escalation paths, audit trails, and human-in-the-loop for automated decisions.

## 5. Architecture
See `architecture_diagram.mmd` and `fault_tree.mmd`. Export to PNG with `docs/generate_artifacts.sh`.

## 6. Runbooks (Excerpt)
- Incident: Exam service outage
  - Detect via elevated 5xx/error rate and failing health checks.
  - Mitigate: circuit breaker opens; shift to degraded mode; notify stakeholders.
  - Rollback: deploy last known good; verify health; postmortem within 48 hours.

## 7. Roadmap
- Introduce resilient clients via Resilience4j.
- Blue/green + canary releases for exam service.
- Add async ingestion and backpressure controls.
- Synthetic monitoring during exam windows.
