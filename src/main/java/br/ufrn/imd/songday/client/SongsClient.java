package br.ufrn.imd.songday.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@HttpExchange("songs-reactive")
public interface SongsClient {
    @GetExchange("/songs/{id}")
    public Flux<Object> findById(@PathVariable String id);

    @PutExchange("/songpopularities/score")
    public Mono<Void> updateScore(@RequestParam(name = "songId") String songId);
}
