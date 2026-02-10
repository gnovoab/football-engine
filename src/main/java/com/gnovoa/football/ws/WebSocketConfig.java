package com.gnovoa.football.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WsRouter router;

    public WebSocketConfig(WsRouter router) {
        this.router = router;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(router, "/ws/matches/*")
                .setAllowedOrigins("*");

        registry.addHandler(router, "/ws/leagues/*/fixtures/*")
                .setAllowedOrigins("*");
    }
}