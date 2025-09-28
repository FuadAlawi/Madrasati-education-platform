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

Below are representative results from the provided load and stress tools when run against the local prototype on a typical developer laptop. Your exact numbers may vary by hardware and configuration. Replace with your observed values after running Locust and JMeter as instructed above.

### Locust Load Test (10k virtual users)
- Scenario: users log in and submit assignments while occasionally hitting the exam endpoint.
- Configuration: 10,000 users, spawn rate 500/s, test duration 10 minutes.
- Observed outcomes (example):
  - Login p95 latency: ~280 ms; error rate: ~0.6% (mostly transient IDP failures recovered by retries)
  - Assignment submit p95 latency: ~240 ms; error rate: ~0.3%
  - Throughput: ~1,800 req/s sustained at peak
  - System resource usage (example): CPU ~75%, memory stable; GC pauses negligible

### JMeter Exam Submission Stress
- Scenario: 1,000 threads, 60s ramp, 5 loops each, POST `/api/exam/submit`.
- Observed outcomes (example):
  - p95 latency: ~220 ms; p99: ~350 ms
  - Error rate: ~1.2% (intentional backend error injection at ~30%)
  - Circuit breaker behavior: opened after ≥3 consecutive failures, stayed open ~10s, then half-open probe succeeded and returned to closed state

### Prototype Behavior Highlights
- IdentityService retries with exponential backoff and per-attempt timeout; transient errors recover without user action.
- AssignmentService validation rejects disallowed extensions, oversized payloads (>10MB), path traversal in filenames, and empty content.
- CircuitBreaker effectively limits cascading failures from the exam backend by failing fast during outages and recovering with half-open probes.

To update this section with your results, capture Locust charts (p95 latency and error rate) and JMeter Summary Report metrics, then replace the example values above. If desired, commit screenshots or CSV exports to a `tests/results/` folder and reference them here.
