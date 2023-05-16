package br.ufrn.imd.songday.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.songday.dto.user.UserInput;
import br.ufrn.imd.songday.dto.user.UserMapper;
import br.ufrn.imd.songday.dto.user.UserOutput;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.service.UserService;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private UserService service;

    @Autowired
    private UserMapper mapper;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<UserOutput>> save(@Valid @RequestBody UserInput userInput) {
        Mono<User> newUser = service.createUser(mapper.toUser(userInput));
        return newUser.map(mapper::toUserOutput)
                .map(ResponseEntity::ok);
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserOutput> getAll(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAll(pageable)
                .map(mapper::toUserOutput);
    }

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<UserOutput>> getById(@PathVariable String id) {
        Mono<User> user = service.findById(id);
        return user.map(mapper::toUserOutput)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/{idFollowee}/follow", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<String>> follow(@PathVariable String idFollowee, @RequestBody String userId) {
        return service.follow(idFollowee, userId)
                .map(x -> ResponseEntity.ok("Usuário seguido com sucesso"));
    }

    @PostMapping(value = "/{idFollowee}/unfollow", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<String>> unfollow(@PathVariable String idFollowee, @RequestBody String userId) {
        return service.unfollow(idFollowee, userId)
                .map(x -> ResponseEntity.ok("Deixou de seguir com sucesso"));
    }

    @GetMapping(value = "/username/{username}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<UserOutput>> getByUsername(@PathVariable String username) {
        Mono<User> user = service.findByUsername(username);
        return user.map(mapper::toUserOutput)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/username/{username}/followees", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Set<String>>> getFolloweesByUsername(@PathVariable String username) {
        Mono<User> user = service.findByUsername(username);
        return user.map(u -> u.getFollowees())
                .map(ResponseEntity::ok);
    }
}
