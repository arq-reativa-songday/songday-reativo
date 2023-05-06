package br.ufrn.imd.songday.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.model.Comment;
import br.ufrn.imd.songday.repository.CommentRepository;
import br.ufrn.imd.songday.repository.PostReadOnlyRepository;
import br.ufrn.imd.songday.repository.UserReadOnlyRepository;

@Service
public class CommentService {
    @Autowired
    private CommentRepository repository;

    @Autowired
    private UserReadOnlyRepository userReadOnlyRepository;

    @Autowired
    private PostReadOnlyRepository postReadOnlyRepository;

    public Comment save(Comment newComment, String postId) {
        userReadOnlyRepository.findById(newComment.getUserId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        postReadOnlyRepository.findById(postId).orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
        newComment.setPostId(postId);

        return repository.save(newComment);
    }

    public void delete(String postId, String commentId) {
        Optional<Comment> comment = repository.findByIdAndPostId(commentId, postId);
        if (!comment.isPresent()) {
            throw new NotFoundException("Comentário não encontrado");
        }

        repository.deleteById(commentId);
    }
}
