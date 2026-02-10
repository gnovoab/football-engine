
# ⚽ Football Simulator

A high-performance **Spring Boot** application that simulates three major football leagues in real-time. The engine manages match logic, timing, and events, streaming every moment via **WebSockets**.

---

## 🚀 Key Features

### 🏆 Multi-League Support

Simulates **Premier League**, **Serie A**, and **La Liga** simultaneously.

* **League Structure:** 20 teams per league, 38 rounds (Double Round-Robin).
* **Simultaneous Play:** All 10 matches per fixture are simulated at once.

### ⏱️ Real-Time Match Engine

* **Half Duration:** 45 minutes of simulated time.
* **Intervals:** 30-second half-time; 10-minute gaps between fixtures; 1-hour gaps between seasons.
* **Dynamic Stoppage Time:** Basic injury/extra time calculations.
* **Match Events:** High-fidelity event generation including:
* Shots, Saves, Goals, and Corners.
* Yellow/Red Cards and Substitutions.
* VAR Checks.
* **Snapshots:** A full `MATCH_SNAPSHOT` is broadcast every 10 seconds.



---

## 🛠️ Requirements & Tech Stack

* **Java:** 21
* **Build Tool:** Gradle
* **Communication:** WebSockets & REST

---

## 🏁 Getting Started

### 1. Generate Rosters

The application requires dummy rosters to run. Use the provided tool to generate them:

```bash
# Create directory and compile/run the generator
mkdir -p rosters
javac tools/RosterGenerator.java
java -cp tools RosterGenerator

```

**Output Files Created:**

* `./rosters/premier-league.json`
* `./rosters/serie-a.json`
* `./rosters/la-liga.json`

### 2. Configure Environment

Edit `src/main/resources/application.yml` to tune the simulation:

* `sim.rosters.baseDir`: Path to your roster folder.
* `runner.*`: Configure the number of seasons and gap timings.

### 3. Run the App

```bash
./gradlew build
./gradlew bootRun

```

The application will be available at: **`http://localhost:8080`**

---

## 🔌 API Documentation

Explore the interactive Swagger UI or download the definitions:

* **Swagger UI:** [http://localhost:8080/swagger-ui.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui.html)
* **OpenAPI Specs:** [JSON](https://www.google.com/search?q=http://localhost:8080/v3/api-docs) | [YAML](https://www.google.com/search?q=http://localhost:8080/v3/api-docs.yaml)

---

## 🛰️ REST API Endpoints

Use the following league keys: `PREMIER_LEAGUE`, `SERIE_A`, `LA_LIGA`.

### Runner Control

| Action | Method | Endpoint |
| --- | --- | --- |
| **Pause** | `POST` | `/api/runners/{league}/pause` |
| **Resume** | `POST` | `/api/runners/{league}/resume` |
| **Stop** | `POST` | `/api/runners/{league}/stop` |

### League Information

| Resource | Method | Endpoint |
| --- | --- | --- |
| **Current Fixture** | `GET` | `/api/runners/{league}/running-fixture` |
| **Full Schedule** | `GET` | `/api/runners/{league}/schedule` |

---

## 📡 WebSocket Streams

To connect, prefix these paths with `ws://localhost:8080`.

* **Full Fixture Stream:** `/ws/leagues/{league}/fixtures/{fixtureId}`
* *Streams events for all 10 matches in a fixture.*


* **Single Match Stream:** `/ws/matches/{matchId}`
* *Focus on a specific match.*



> **Note:** Call the `running-fixture` REST endpoint first to retrieve the active `fixtureId` and `matchId`.

---

## 📝 Technical Notes

* **Data Persistence:** Rosters are loaded from JSON files into memory. No database is required.
* **Replayability:** Simulations use a randomized engine; match outcomes are non-replayable.
* **Updates:** Roster changes require an application restart to take effect.

---


