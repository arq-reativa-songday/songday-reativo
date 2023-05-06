package br.ufrn.imd.songday.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.songday.dto.comment.CommentInput;
import br.ufrn.imd.songday.dto.comment.CommentMapper;
import br.ufrn.imd.songday.dto.comment.CommentOutput;
import br.ufrn.imd.songday.model.Comment;
import br.ufrn.imd.songday.service.CommentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("posts/{postId}/comments")
public class CommentController {
    @Autowired
    private CommentService service;

    @Autowired
    private CommentMapper mapper;

    @PostMapping
    public ResponseEntity<CommentOutput> save(@PathVariable String postId, @Valid @RequestBody CommentInput commentInput) {
        Comment newComment = service.save(mapper.toComment(commentInput), postId);
        return ResponseEntity.ok(mapper.toCommentOutput(newComment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> save(@PathVariable String postId, @PathVariable("id") String commentId) {
        service.delete(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
