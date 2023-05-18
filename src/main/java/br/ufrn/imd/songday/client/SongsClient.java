package br.ufrn.imd.songday.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.ufrn.imd.songday.exception.ServicesCommunicationException;
import reactor.core.publisher.Mono;

@Component
public class SongsClient {
    @Autowired
    private WebClient webClient;

    public Mono<Boolean> findById(String id) {
        return webClient
                .get()
                .uri("/songs/{id}", id)
                .exchangeToMono(response -> {
                    return response.statusCode().is2xxSuccessful() ? Mono.just(Boolean.TRUE) : Mono.just(Boolean.FALSE);
                })
                .onErrorResume(throwable -> {
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com Songs para recuperar a música por id"));
                });
    }

    public Mono<Void> updateScore(String songId) {
        return webClient
                .put()
                .uri(builder -> builder
                        .path("/songpopularities/score")
                        .queryParam("songId", songId)
                        .build())
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(Void.class);
                    }
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com Songs para atualizar o score da música"));
                })
                .onErrorResume(throwable -> {
                    return Mono.error(new ServicesCommunicationException(
                            "Erro durante a comunicação com Songs para atualizar o score da música"));
                });
    }
}
