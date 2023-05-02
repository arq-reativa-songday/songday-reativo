package br.ufrn.imd.songday.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.ufrn.imd.songday.model.Post;

public interface PostRepository extends MongoRepository<Post, String> {
    
}
