package com.srm.machinemonitor.Configs;

import com.srm.machinemonitor.Handlers.WebSocketHandlers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Value("${clientDomainName}")
    String clientDomain;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandlers(), "/ws").setAllowedOriginPatterns(clientDomain);
        registry.addHandler(new WebSocketHandlers(), "/ws").setAllowedOriginPatterns(clientDomain).withSockJS();
    }

}
