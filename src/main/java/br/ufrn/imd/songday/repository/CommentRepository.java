package br.ufrn.imd.songday.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.ufrn.imd.songday.model.Comment;
import java.util.Optional;


public interface CommentRepository extends MongoRepository<Comment, String> {
    Optional<Comment> findByIdAndPostId(String id, String postId);
}
