package br.ufrn.imd.songday.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.model.Comment;
import br.ufrn.imd.songday.model.Post;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.CommentRepository;
import br.ufrn.imd.songday.repository.PostReadOnlyRepository;
import br.ufrn.imd.songday.repository.UserReadOnlyRepository;
import reactor.core.publisher.Mono;

@Service
public class CommentService {
    @Autowired
    private CommentRepository repository;

    @Autowired
    private UserReadOnlyRepository userReadOnlyRepository;

    @Autowired
    private PostReadOnlyRepository postReadOnlyRepository;

    public Mono<Comment> save(Comment newComment, String postId) {
        Mono<User> user = userReadOnlyRepository.findById(newComment.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("Usuário não encontrado")));

        Mono<Post> post = postReadOnlyRepository.findById(postId)
                .switchIfEmpty(Mono.error(new NotFoundException("Publicação não encontrada")));

        return user.zipWith(post).flatMap(t -> {
            newComment.setPostId(postId);
            return repository.save(newComment);
        });
    }

    public Mono<Void> delete(String postId, String commentId) {
        return repository.findByIdAndPostId(commentId, postId)
                .switchIfEmpty(Mono.error(new NotFoundException("Comentário não encontrado")))
                .flatMap(commentFound -> repository.deleteById(commentId));
    }
}
