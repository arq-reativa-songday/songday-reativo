package br.ufrn.imd.songday.repository;

import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface ReadOnlyRepository<T> extends Repository<T, String> {
    Optional<T> findById(String id);
}
