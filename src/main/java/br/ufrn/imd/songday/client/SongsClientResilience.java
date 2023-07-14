package br.ufrn.imd.songday.client;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class SongsClientResilience {

    @Autowired
    private SongsClient songsClient;

    @CircuitBreaker(name = "songs-find-by-id")
    @Bulkhead(name = "songs-find-by-id-bulkhead")
//    @RateLimiter(name = "songs-find-by-id-rate-limiter")
    @Retry(name = "songs-find-by-id-retry")

    public Flux<Object> findById(String songId) {
        Flux<Object> byId = songsClient.findById(songId);
        return byId;
    }
}
