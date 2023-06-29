package br.ufrn.imd.songday.cache.template;

import reactor.core.publisher.Mono;

public abstract class CacheTemplate<KEY, ENTITY> {

    public Mono<ENTITY> get(KEY key) {
        return getFromCache(key)
                .switchIfEmpty(
                        getFromSource(key)
                                .flatMap(e -> updateCache(key, e)))
                .doFirst(() -> {
                    System.out.println("Obtendo chave " + key + "...");
                });
    }

    public Mono<ENTITY> update(KEY key, ENTITY entity) {
        return updateSource(key, entity)
                .flatMap(e -> deleteFromCache(key).thenReturn(e))
                .doFirst(() -> {
                    System.out.println("Atualizando chave " + key + "...");
                });
    }

    public Mono<Void> delete(KEY key) {
        return deleteFromSource(key)
                .then(deleteFromCache(key))
                .doFirst(() -> {
                    System.out.println("Excluindo chave " + key + "...");
                });
    }

    abstract protected Mono<ENTITY> getFromSource(KEY key);

    abstract protected Mono<ENTITY> getFromCache(KEY key);

    abstract protected Mono<ENTITY> updateSource(KEY key, ENTITY entity);

    abstract protected Mono<ENTITY> updateCache(KEY key, ENTITY entity);

    abstract protected Mono<Void> deleteFromSource(KEY key);

    abstract protected Mono<Void> deleteFromCache(KEY key);

}
