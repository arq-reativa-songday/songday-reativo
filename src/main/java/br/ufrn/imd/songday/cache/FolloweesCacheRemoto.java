package br.ufrn.imd.songday.cache;

import java.util.Set;

import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufrn.imd.songday.cache.template.CacheTemplate;
import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.repository.UserRepository;
import reactor.core.publisher.Mono;

@Component
public class FolloweesCacheRemoto extends CacheTemplate<String, Set<String>> {
    @Autowired
    private UserRepository repository;
    private RMapCacheReactive<String, Set<String>> map;

    public FolloweesCacheRemoto(RedissonReactiveClient redissonClient) {
        Codec codec = new TypedJsonJacksonCodec(String.class, Set.class);
        this.map = redissonClient.getMapCache("/followees-reativo-remoto/", codec);
    }

    @Override
    protected Mono<Set<String>> getFromSource(String key) {
        return repository.findByUsername(key)
                .switchIfEmpty(
                        Mono.error(new NotFoundException(String.format("O usuário '%s' não foi encontrado", key))))
                .map(u -> u.getFollowees())
                .doFirst(() -> {
                    System.out.println("Buscando chave " + key + " no banco...");
                });
    }

    @Override
    protected Mono<Set<String>> getFromCache(String key) {
        return map.get(key).doFirst(() -> {
            System.out.println("Buscando chave " + key + " no cache (remoto)...");
        });
    }

    @Override
    protected Mono<Set<String>> updateSource(String key, Set<String> entity) {
        //
        return Mono.just(entity);
    }

    @Override
    protected Mono<Set<String>> updateCache(String key, Set<String> entity) {
        return this.map.fastPut(key, entity).thenReturn(entity)
                .doFirst(() -> {
                    System.out.println("Atualizando chave " + key + " no cache (remoto)");
                });
    }

    @Override
    protected Mono<Void> deleteFromSource(String key) {
        //
        return Mono.empty();
    }

    @Override
    protected Mono<Void> deleteFromCache(String key) {
        return this.map.fastRemove(key).then()
                .doFirst(() -> {
                    System.out.println("Excluindo chave " + key + " do cache (remoto)");
                });
    }
}
