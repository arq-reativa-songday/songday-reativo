package br.ufrn.imd.songday.repository;

import java.util.Date;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.ufrn.imd.songday.model.Post;

public interface PostRepository extends MongoRepository<Post, String> {
    boolean existsByUserIdAndCreatedAtBetween(String userId, Date start, Date end);
}
