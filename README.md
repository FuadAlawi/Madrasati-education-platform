# Madrasati Reliability Strategy Prototype

## Overview
This repository contains a complete reliability strategy prototype for the Madrasati education platform. It includes:

- Architecture and fault tree diagrams (Mermaid sources in `docs/` with scripts to export PNGs)
- Java microservice-style sample components with resilience patterns (Spring Boot minimal REST)
- Circuit breaker implementation for the exam service
- Robust input validation for assignment submissions
- Login with retry and timeout logic
- Load testing with Locust and a JMeter test plan
- PDF report (Markdown source in `docs/` with instructions to export to PDF)

## Repository Structure
- `src/` → Java Spring Boot application and services
- `tests/` → Load test scripts: Locust and JMeter
- `docs/` → Report (Markdown source), diagrams (Mermaid), humanization checklist, export script

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Python 3.9+ (for Locust)
- Node.js + `@mermaid-js/mermaid-cli` OR Docker (for diagram export)
- Pandoc + a PDF engine (wkhtmltopdf or LaTeX) OR Docker (for report export)
- Apache JMeter (to run the `.jmx` test plan)

### Run the Application
```bash
mvn spring-boot:run
```
The app starts on `http://localhost:8080`.

### Key Endpoints
- `POST /api/auth/login` — login with retry and timeout logic
  - Body: `{ "username": "student", "password": "secret" }`
- `POST /api/assignments/submit` — assignment submission with validation
  - Multipart form fields: `studentId`, `courseId`, `filename`, `content` (base64 or simple text for prototype)
- `POST /api/exam/submit` — exam submission path guarded by circuit breaker

### Run Locust (Load Test)
Install Locust:
```bash
pip install locust
```
Run:
```bash
locust -f tests/locustfile.py --host=http://localhost:8080
```
Use the web UI (by default at `http://localhost:8089`) to set users (e.g., 10000) and hatch rate.

### Run JMeter Test
Open `tests/jmeter_exam_submission.jmx` in JMeter, set target host to `localhost` and port to `8080`, then run the plan.

## Export Diagrams (PNG) and Report (PDF)
We store sources in:
- `docs/architecture_diagram.mmd`
- `docs/fault_tree.mmd`
- `docs/Reliability_Strategy_Report.md`

Use the helper script:
```bash
bash docs/generate_artifacts.sh
```
This script will:
- Export `architecture_diagram.png` and `fault_tree.png` to `docs/` using Mermaid CLI
- Export `Reliability_Strategy_Report.pdf` to `docs/` using Pandoc

If you prefer Docker (no local installs):
```bash
bash docs/generate_artifacts.sh docker
```

## Reliability Metrics Explained (Summary)
- MTBF = Total uptime / Number of failures
- MTTR = Total downtime / Number of failures
- Availability = MTBF / (MTBF + MTTR)
- POFOD = Failures / Demands
- ROCOF = Failures / Time interval

Targets and details are in `docs/Reliability_Strategy_Report.md`.

## Simulation and Prototype Results

Below are the actual results measured on this machine using Locust headless mode. CSV outputs are saved under `tests/results/` and committed.

- Raw CSVs:
  - `tests/results/locust_stats.csv`
  - `tests/results/locust_stats_history.csv`
  - `tests/results/locust_failures.csv`
  - `tests/results/locust_exceptions.csv`

### Locust Load Test (Actual)
- Scenario: users log in and submit assignments while also hitting the exam endpoint.
- Configuration: 2,000 users, spawn rate 200/s, run-time 2 minutes.
- Observed outcomes (from `locust_stats.csv`):
  - Aggregated throughput: ~760 req/s; total requests: 88,422; total failures: 21,085 (23.84%)
  - Assignment submit (`POST /api/assignments/submit`):
    - Requests: 64,892; Failures: 0 (0.00%)
    - Median: 1 ms; Average: 301 ms; p95: 11 ms; p99: 14,000 ms; Max: 21,777 ms
    - Throughput: ~558 req/s
  - Login (`POST /api/auth/login`):
    - Requests: 2,000; Failures: 0 (0.00%)
    - Median: 13,000 ms; Average: 13,361 ms; p95: 25,000 ms; Max: 25,376 ms
    - Throughput: ~17 req/s
  - Exam submit (`POST /api/exam/submit`):
    - Requests: 21,530; Failures: 21,085 (97.93%) — expected due to intentional backend error injection and circuit breaker rejecting during OPEN state
    - Median: 1 ms; Average: 301 ms; p95: 11 ms; p99: 14,000 ms; Max: 21,531 ms
    - Throughput: ~185 req/s; Failures/s: ~181

### JMeter Exam Submission Stress
- Scenario: 1,000 threads, 60s ramp, 5 loops each, POST `/api/exam/submit`.

#### Actual JMeter Results (Java)
- Command executed:
  - `jmeter -n -t tests/jmeter_exam_submission.jmx -l tests/results/jmeter_results.jtl -e -o tests/results/jmeter_report`
- Summary (from terminal):
  - Total samples: 5,000 in 60s (~83.4/s)
  - Average: ~0–1 ms, Min: 0 ms, Max: 102 ms
  - Errors: 4,927 (98.54%)
  - Reason: exam backend injects ~30% failures and circuit breaker opens (returns HTTP 503 fast), producing many intentional errors during OPEN state.
- Outputs committed:
  - `tests/results/jmeter_results.jtl`
  - `tests/results/jmeter_report/` (HTML report)

#### Proof of Actual Execution (JMeter)
- Terminal output saved to: `tests/results/jmeter_terminal.txt`
```
Creating summariser <summary>
Created the tree successfully using tests/jmeter_exam_submission.jmx
Starting standalone test @ 2025 Sep 28 04:09:40 AST
summary =   5000 in 00:01:00 =   83.4/s Avg:     0 Min:     0 Max:   102 Err:  4927 (98.54%)
Tidying up ...    @ 2025 Sep 28 04:10:40 AST
... end of run
```

### Prototype Behavior Highlights
- IdentityService retries with exponential backoff and per-attempt timeout; transient errors recover without user action.
- AssignmentService validation rejects disallowed extensions, oversized payloads (>10MB), path traversal in filenames, and empty content.
- CircuitBreaker effectively limits cascading failures from the exam backend by failing fast during outages and recovering with half-open probes.

Notes:
- The high login latency reflects the simulated identity service behavior under heavy concurrent load and retry/timeout logic. You can tune `perAttemptTimeout`, backoff, and max retries in `IdentityService` to adjust.
- The very high failure rate on the exam endpoint is expected due to the intentionally injected ~30% backend error rate combined with the circuit breaker opening (rejecting calls with HTTP 503) during outages. This validates the breaker behavior.

### Proof of Actual Execution
- Locust terminal output captured from the real run: `tests/results/locust_terminal.txt`
- Raw CSV outputs (stats, failures, history) are included in `tests/results/`.

```
[2025-09-28 03:55:17,492] FUADs-MacBook-Pro/INFO/locust.main: Starting Locust 2.41.1
[2025-09-28 03:55:17,500] FUADs-MacBook-Pro/INFO/locust.main: Run time limit set to 120 seconds
[2025-09-28 03:55:17,501] FUADs-MacBook-Pro/WARNING/locust.runners: Your selected spawn rate is very high (>100), and this is known to sometimes cause issues. Do you really need to ramp up that fast?
[2025-09-28 03:55:17,501] FUADs-MacBook-Pro/INFO/locust.runners: Ramping to 2000 users at a rate of 200.00 per second
[2025-09-28 03:55:26,540] FUADs-MacBook-Pro/INFO/locust.runners: All users spawned: {"StudentUser": 2000} (2000 total users)
[2025-09-28 03:57:13,851] FUADs-MacBook-Pro/INFO/locust.main: --run-time limit reached, shutting down
[2025-09-28 03:57:14,004] FUADs-MacBook-Pro/INFO/locust.main: Shutting down (exit code 1)
Type     Name  # reqs      # fails |    Avg     Min     Max    Med |   req/s  failures/s
--------||-------|-------------|-------|-------|-------|-------|--------|-----------
POST     /api/assignments/submit   64944     0(0.00%) |    301       0   21777      1 |  557.61        0.00
POST     /api/auth/login    2000     0(0.00%) |  13361     325   25376  13000 |   17.17        0.00
POST     /api/exam/submit   21545 21100(97.93%) |    301       0   21531      1 |  184.98      181.16
--------||-------|-------------|-------|-------|-------|-------|--------|-----------
         Aggregated   88489 21100(23.84%) |    596       0   25376      1 |  759.76      181.16

Response time percentiles (approximated)
Type     Name      50%    66%    75%    80%    90%    95%    98%    99%  99.9% 99.99%   100% # reqs
--------||--------|------|------|------|------|------|------|------|------|------|------|------
POST     /api/assignments/submit        1      2      2      2      4     12   6900  14000  21000  21000  22000  64944
POST     /api/auth/login    13000  17000  20000  20000  23000  25000  25000  25000  25000  25000  25000   2000
POST     /api/exam/submit        1      2      2      2      4     12   6900  14000  21000  21000  22000  21545
--------||--------|------|------|------|------|------|------|------|------|------|------|------
         Aggregated        1      2      2      3      5     52  14000  18000  25000  25000  25000  88489

Error report
# occurrences      Error
------------------|-------------------------------------------------------------
21100              POST /api/exam/submit: HTTPError('503 Server Error:  for url: /api/exam/submit')
------------------|-------------------------------------------------------------
```
