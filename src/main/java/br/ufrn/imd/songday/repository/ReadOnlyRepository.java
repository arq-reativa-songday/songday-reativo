package br.ufrn.imd.songday.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface ReadOnlyRepository<T> extends Repository<T, String> {
    Mono<T> findById(String id);
}
