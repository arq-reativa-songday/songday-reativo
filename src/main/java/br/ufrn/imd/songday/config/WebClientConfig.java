package br.ufrn.imd.songday.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import br.ufrn.imd.songday.client.SongsClient;
import br.ufrn.imd.songday.exception.ServicesCommunicationException;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    @Value("${songs.api.address}")
    private String baseUrl;

    @Bean
    SongsClient songsClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(
                        httpStatusCode -> HttpStatus.NOT_FOUND == httpStatusCode,
                        response -> Mono.empty())
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServicesCommunicationException(
                                "Erro durante a comunicação com Songs: " + response.toString())))
                .build();

        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build()
                .createClient(SongsClient.class);
    }
}
