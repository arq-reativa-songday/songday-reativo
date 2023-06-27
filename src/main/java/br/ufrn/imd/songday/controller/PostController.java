package br.ufrn.imd.songday.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.songday.dto.post.PostInput;
import br.ufrn.imd.songday.dto.post.PostMapper;
import br.ufrn.imd.songday.dto.post.PostSearchDto;
import br.ufrn.imd.songday.dto.post.SearchPostsCountDto;
import br.ufrn.imd.songday.dto.post.SearchPostsDto;
import br.ufrn.imd.songday.model.Post;
import br.ufrn.imd.songday.service.PostService;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("posts")
public class PostController {
    @Autowired
    private PostService service;

    @Autowired
    private PostMapper mapper;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Post>> save(@Valid @RequestBody Mono<PostInput> postInput) {
        Mono<Post> newPost = service.createPost(postInput.map(mapper::toPost));
        return newPost.map(ResponseEntity::ok);
    }

    @PostMapping(value = "/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PostSearchDto> getAll(@Valid @RequestBody Mono<SearchPostsDto> search) {
        return service.findAll(search);
    }

    @PostMapping(value = "/{id}/like", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<String>> like(@PathVariable String id, @RequestBody Mono<String> userId) {
        return service.like(id, userId)
                .map(x -> ResponseEntity.ok("Publicação curtida com sucesso"));
    }

    @PostMapping(value = "/{id}/unlike", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<String>> unlike(@PathVariable String id, @RequestBody Mono<String> userId) {
        return service.unlike(id, userId)
                .map(x -> ResponseEntity.ok("Deixou de curtir com sucesso"));
    }

    @PostMapping(value = "/search/count", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Long>> searchPostsCount(@Valid @RequestBody Mono<SearchPostsCountDto> search) {
        return service.searchPostsCount(search)
                .map(ResponseEntity::ok);
    }
}
