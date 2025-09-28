#!/usr/bin/env bash
set -euo pipefail
MODE=${1:-local}
ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
DOCS_DIR="$ROOT_DIR/docs"

ARCH_SRC="$DOCS_DIR/architecture_diagram.mmd"
FAULT_SRC="$DOCS_DIR/fault_tree.mmd"
ARCH_PNG="$DOCS_DIR/architecture_diagram.png"
FAULT_PNG="$DOCS_DIR/fault_tree.png"
REPORT_MD="$DOCS_DIR/Reliability_Strategy_Report.md"
REPORT_PDF="$DOCS_DIR/Reliability_Strategy_Report.pdf"

if [ "$MODE" = "docker" ]; then
  echo "Exporting diagrams via Docker mermaid-cli..."
  docker run --rm -v "$DOCS_DIR":"/data" minlag/mermaid-cli mmdc -i /data/architecture_diagram.mmd -o /data/architecture_diagram.png
  docker run --rm -v "$DOCS_DIR":"/data" minlag/mermaid-cli mmdc -i /data/fault_tree.mmd -o /data/fault_tree.png
  echo "Exporting PDF via Docker pandoc..."
  docker run --rm -v "$DOCS_DIR":"/data" pandoc/latex:latest /data/Reliability_Strategy_Report.md -o /data/Reliability_Strategy_Report.pdf
else
  echo "Exporting diagrams locally (requires @mermaid-js/mermaid-cli)..."
  if ! command -v mmdc >/dev/null 2>&1; then
    echo "Mermaid CLI (mmdc) not found. Install with: npm i -g @mermaid-js/mermaid-cli" >&2
    exit 1
  fi
  mmdc -i "$ARCH_SRC" -o "$ARCH_PNG"
  mmdc -i "$FAULT_SRC" -o "$FAULT_PNG"
  echo "Exporting PDF locally (requires pandoc)..."
  if ! command -v pandoc >/dev/null 2>&1; then
    echo "pandoc not found. Install pandoc or run: bash docs/generate_artifacts.sh docker" >&2
    exit 1
  fi
  pandoc "$REPORT_MD" -o "$REPORT_PDF"
fi

echo "Artifacts generated:"
echo " - $ARCH_PNG"
echo " - $FAULT_PNG"
echo " - $REPORT_PDF"
