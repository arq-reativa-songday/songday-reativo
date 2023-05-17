package br.ufrn.imd.songday.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import br.ufrn.imd.songday.model.Comment;
import reactor.core.publisher.Mono;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    Mono<Comment> findByIdAndPostId(String id, String postId);
}
