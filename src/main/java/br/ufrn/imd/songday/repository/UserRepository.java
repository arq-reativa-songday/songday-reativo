package br.ufrn.imd.songday.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import br.ufrn.imd.songday.model.User;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUsername(String username);
}
