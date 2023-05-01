package br.ufrn.imd.songday.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.ufrn.imd.songday.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}
