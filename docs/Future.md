2) Make sure the repo has a single source of truth for config
   Keep application.yml committed with defaults (paths, timings). That way “what the system does” is visible and repeatable.

3) If you want “future changes” to be easy
   Tell me what you want to change next time (examples):
   “Add real player names from a provider”
   “Tune event rates”
   “Add league-level WS events”
   “Make rosters reloadable without restart”
   “Deploy”
   And when you come back, send:
   docs/DECISIONS.md
   application.yml
   the file you want to change
   That’s enough context for me to help accurately.