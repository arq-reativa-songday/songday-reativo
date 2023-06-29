package br.ufrn.imd.songday.cache;

import java.util.Set;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMapReactive;
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
public class FolloweesCacheLocal extends CacheTemplate<String, Set<String>> {
    @Autowired
    private UserRepository repository;
    private RLocalCachedMapReactive<String, Set<String>> map;

    public FolloweesCacheLocal(RedissonReactiveClient redissonClient) {
        Codec codec = new TypedJsonJacksonCodec(String.class, Set.class);
        LocalCachedMapOptions<String, Set<String>> options = LocalCachedMapOptions.<String, Set<String>>defaults()
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LFU)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);

        this.map = redissonClient.getLocalCachedMap("/followees-reativo-local/", codec, options);
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
            System.out.println("Buscando chave " + key + " no cache (local)...");
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
                    System.out.println("Atualizando chave " + key + " no cache (local)");
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
                    System.out.println("Excluindo chave " + key + " do cache (local)");
                });
    }
}
