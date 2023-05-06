package br.ufrn.imd.songday.repository;

import org.springframework.stereotype.Repository;

import br.ufrn.imd.songday.model.User;

@Repository
public interface UserReadOnlyRepository extends ReadOnlyRepository<User> {
    
}
