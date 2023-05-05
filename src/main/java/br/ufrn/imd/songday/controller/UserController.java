package br.ufrn.imd.songday.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private UserService service;

    @Autowired
    private UserMapper mapper;

    @PostMapping
    public ResponseEntity<UserOutput> save(@Valid @RequestBody UserInput userInput) {
        User newUser = service.createUser(mapper.toUser(userInput));
        return ResponseEntity.ok(mapper.toUserOutput(newUser));
    }

    @GetMapping
    public ResponseEntity<Page<UserOutput>> getAll(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserOutput> userPage = service.findAll(pageable).map(user -> mapper.toUserOutput(user));
        return ResponseEntity.ok(userPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserOutput> getById(@PathVariable String id) {
        User user = service.findById(id);
        return ResponseEntity.ok(mapper.toUserOutput(user));
    }

    @PutMapping("/follow/{idFollowee}")
    public ResponseEntity<String> follow(@PathVariable String idFollowee, @AuthenticationPrincipal User user) {
        service.follow(idFollowee, user);
        return ResponseEntity.ok("Usuário seguido com sucesso");
    }

    @PutMapping("/unfollow/{idFollowee}")
    public ResponseEntity<String> unfollow(@PathVariable String idFollowee, @AuthenticationPrincipal User user) {
        service.unfollow(idFollowee, user);
        return ResponseEntity.ok("Deixou de seguir com sucesso");
    }
}
