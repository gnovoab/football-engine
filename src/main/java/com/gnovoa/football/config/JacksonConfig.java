package com.gnovoa.football.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Explicit Jackson configuration.
 *
 * <p>Some setups (especially minimal starter combinations) may not activate
 * Spring Boot's Jackson auto-configuration. This guarantees an ObjectMapper bean
 * is available for constructor injection (e.g. in RosterCatalog).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // good default for Instant/LocalDateTime, etc.
        return mapper;
    }
}