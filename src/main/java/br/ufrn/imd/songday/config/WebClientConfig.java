package br.ufrn.imd.songday.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${songs.api.address}")
    private String baseUrl;

    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
