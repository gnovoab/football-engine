package com.gnovoa.football.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class WsRouter extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String key = routeKey(session.getUri() == null ? "" : session.getUri().getPath());
        sessions.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String key = routeKey(session.getUri() == null ? "" : session.getUri().getPath());
        var set = sessions.get(key);
        if (set != null) set.remove(session);
    }

    public Set<WebSocketSession> forKey(String key) {
        return sessions.getOrDefault(key, Set.of());
    }

    private String routeKey(String path) {
        // /ws/matches/{matchId} -> match:{matchId}
        // /ws/leagues/{league}/fixtures/{fixtureId} -> fixture:{league}:{fixtureId}
        String[] p = path.split("/");
        if (path.contains("/ws/matches/") && p.length >= 4) {
            return "match:" + p[p.length - 1];
        }
        if (path.contains("/ws/leagues/") && path.contains("/fixtures/") && p.length >= 6) {
            String league = p[p.length - 3];
            String fixture = p[p.length - 1];
            return "fixture:" + league + ":" + fixture;
        }
        return "unknown";
    }
}