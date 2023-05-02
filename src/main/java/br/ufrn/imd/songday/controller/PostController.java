package br.ufrn.imd.songday.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.songday.dto.post.PostInput;
import br.ufrn.imd.songday.dto.post.PostMapper;
import br.ufrn.imd.songday.model.Post;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.service.PostService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("posts")
public class PostController {
    @Autowired
    private PostService service;

    @Autowired
    private PostMapper mapper;

    @PostMapping
    public ResponseEntity<Post> save(@Valid @RequestBody PostInput postInput, @AuthenticationPrincipal User user) {
        Post post = mapper.toPost(postInput);
        return ResponseEntity.ok(service.createPost(post, user));
    }
}
