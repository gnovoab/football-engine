package com.gnovoa.football.out;

import com.gnovoa.football.events.MatchEvent;

public interface EventPublisher {
    void publish(MatchEvent event);
}