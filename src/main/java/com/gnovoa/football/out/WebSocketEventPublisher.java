package com.gnovoa.football.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnovoa.football.events.MatchEvent;
import com.gnovoa.football.ws.WsRouter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public final class WebSocketEventPublisher implements EventPublisher {

    private final WsRouter router;
    private final ObjectMapper mapper;

    public WebSocketEventPublisher(WsRouter router, ObjectMapper mapper) {
        this.router = router;
        this.mapper = mapper;
    }

    @Override
    public void publish(MatchEvent event) {
        try {
            String json = mapper.writeValueAsString(event);
            TextMessage msg = new TextMessage(json);

            for (WebSocketSession s : router.forKey("match:" + event.matchId())) {
                if (s.isOpen()) s.sendMessage(msg);
            }

            String fxKey = "fixture:" + event.league().name() + ":" + event.fixtureId();
            for (WebSocketSession s : router.forKey(fxKey)) {
                if (s.isOpen()) s.sendMessage(msg);
            }
        } catch (Exception ignored) {
            // add logging if needed
        }
    }
}