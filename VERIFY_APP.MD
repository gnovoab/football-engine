# 🚀 App Verification & Monitoring Guide

This document outlines the steps to verify that the football simulation engine and orchestrator are running correctly.

---

## 1. Quick Status Check (REST API)
The fastest way to see the "brain" of the app. Open these in your browser or Postman.

### Running Fixture Status
**URL:** `http://localhost:8080/api/runners/PREMIER_LEAGUE/running-fixture`

| Status | Expected Behavior |
| :--- | :--- |
| **`RUNNING`** | Actively simulating. You will see a `fixtureId` and WebSocket paths. |
| **`WAITING_NEXT_FIXTURE`** | Orchestrator is idle, counting down to the next round. |
| **`PAUSED` / `STOPPED`** | The engine is parked. Will not progress until resumed. |

### Schedule Verification
**URL:** `http://localhost:8080/api/runners/PREMIER_LEAGUE/schedule`
* **Goal:** Verify it returns **38 rounds**. If empty, the rosters or scheduler failed to initialize.

---

## 2. Command Line Tools
Use `curl` for quick interaction directly from your terminal.

```bash
# Get current running state
curl http://localhost:8080/api/runners/PREMIER_LEAGUE/running-fixture

# View the full season schedule
curl http://localhost:8080/api/runners/PREMIER_LEAGUE/schedule

# Force the engine to start/resume
curl -X POST http://localhost:8080/api/runners/PREMIER_LEAGUE/resume
```

## 3. Live Event Streams (WebSockets)
Once the status is **`RUNNING`**, you can tap into the live data feed.

### Option A: Full Fixture Stream (All 10 Matches)
Subscribe here to see the entire Premier League round update simultaneously.
* **URL:** `ws://localhost:8080/ws/leagues/PREMIER_LEAGUE/fixtures/<fixtureId>`

**Using `wscat`:**
```bash
wscat -c "ws://localhost:8080/ws/leagues/PREMIER_LEAGUE/fixtures/fx-9145f679-5e6a-491e-8eb6-03a2fcf48d90"
```

Subscribe to a specific match (e.g., **Arsenal vs Wolves**) to reduce noise and focus on individual event data.

* **URL:** `ws://localhost:8080/ws/matches/<matchId>`

**Using `wscat`:**
```bash
wscat -c "ws://localhost:8080/ws/matches/m-0cb0d97c-f531-4225-bc2e-50ce94c7c60d"
```

## 🩺 Troubleshooting & Sanity Checks

* **The 10-Second Rule:** `MATCH_SNAPSHOT` events are emitted every 10 seconds. If the terminal is empty, wait at least 15 seconds before assuming the connection is dead.
* **Protocol Matters:** Always use `ws://` for WebSockets. Attempting to use `http://` will result in a 404 or a failed handshake.
* **Missing Tooling:** If `wscat` is not installed, you can get it globally via NPM:

```bash
npm i -g wscat
```

## 🎉 After-Party

When a fixture ends, the status automatically switches to `WAITING_NEXT_FIXTURE` with a 10-minute countdown until the next set of games begins.
