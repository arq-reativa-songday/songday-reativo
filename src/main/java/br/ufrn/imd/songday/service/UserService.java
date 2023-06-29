package br.ufrn.imd.songday.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.ufrn.imd.songday.cache.FolloweesCacheWrapper;
import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.exception.ValidationException;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    @Value("${api.cached}")
    private Boolean cacheActive;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FolloweesCacheWrapper followeesCache;

    public Mono<User> createUser(User newUser) {
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        return repository.findByUsername(newUser.getUsername())
                .flatMap(search -> Mono.<User>error(new ValidationException("Nome de usuário já existe")))
                .switchIfEmpty(repository.save(newUser));
    }

    public Flux<User> findAll(Pageable pageable) {
        return repository.findAll()
                .skip(pageable.getPageSize() * pageable.getPageNumber())
                .take(pageable.getPageSize())
                .switchIfEmpty(Mono.error(new NotFoundException("Nenhum usuário encontrado")));
    }

    public Mono<User> findById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuário não encontrado")));
    }

    public Mono<User> follow(String idFollowee, String userId) {
        Mono<User> user = findById(userId);

        return user.flatMap(userFound -> {
            if (idFollowee.equals(userFound.getId())) {
                return Mono.error(new ValidationException("Não é possível seguir seu próprio usuário"));
            }

            boolean hasIdFollowee = userFound.getFollowees().contains(idFollowee);
            if (hasIdFollowee) {
                return Mono.error(new ValidationException("Não é possível seguir um usuário mais de uma vez"));
            }

            return findById(idFollowee).flatMap(followee -> {
                userFound.getFollowees().add(idFollowee);
                return repository.save(userFound)
                        .doOnNext(u -> {
                            if (cacheActive)
                                followeesCache.delete(u.getUsername()).subscribe();
                        });
            });
        });
    }

    public Mono<User> unfollow(String idFollowee, String userId) {
        Mono<User> user = findById(userId);

        return user.flatMap(userFound -> {
            boolean hasIdFollowee = userFound.getFollowees().contains(idFollowee);
            if (!hasIdFollowee) {
                return Mono.error(new ValidationException("Usuário não seguido"));
            }

            userFound.getFollowees().remove(idFollowee);
            return repository.save(userFound)
                    .doOnNext(u -> {
                        if (cacheActive)
                            followeesCache.delete(u.getUsername()).subscribe();
                    });
        });
    }

    public Mono<User> findByUsername(String username) {
        return repository.findByUsername(username)
                .switchIfEmpty(Mono
                        .error(new NotFoundException(String.format("O usuário '%s' não foi encontrado", username))));
    }

    public Mono<Set<String>> findFolloweesByUsername(String username) {
        if (cacheActive) {
            return followeesCache.get(username);
        }

        return findByUsername(username).map(u -> u.getFollowees());
    }
}
