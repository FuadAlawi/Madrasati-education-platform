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

## GitHub-Ready
- Clean repo structure
- Build via Maven
- Scripts for generating diagrams and report
- Clear README and docs

You can push this directory directly to GitHub.
