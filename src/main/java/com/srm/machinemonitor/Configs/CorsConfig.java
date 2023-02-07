package com.srm.machinemonitor.Configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

    @Value("${clientDomainName}")
    String clientDomain;

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList(clientDomain)); // Provide list of origins if you want multiple origins
        config.setAllowedHeaders(Arrays.asList("Content-Type",
                "X-Requested-With",
                "accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-XSRF-TOKEN", "SESSION",
                "Upgrade",
                "Connection",
                "Sec-WebSocket-Extensions"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);

        config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList(clientDomain)); // Provide list of origins if you want multiple origins
        config.setAllowedHeaders(Arrays.asList("Content-Type",
                "X-Requested-With",
                "accept",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-XSRF-TOKEN",
                "Sec-WebSocket-Key",
                "Upgrade",
                "Connection",
                "Sec-WebSocket-Extensions"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH", "OPTIONS"));
        source.registerCorsConfiguration("/ws", config);
        return new CorsFilter(source);
    }

}
