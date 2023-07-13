package br.ufrn.imd.songday.service;

import java.util.Set;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.ufrn.imd.songday.dto.post.GenerateFeedDto;
import br.ufrn.imd.songday.dto.post.PostSearchDto;
import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.repository.PostRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class PostFunction {
    @Autowired
    private PostRepository repository;

    @Autowired
    private UserService userService;

    @Bean
    public Function<Flux<GenerateFeedDto>, Flux<PostSearchDto>> retrievePosts() {
        return input -> {
            return input.flatMap(filter -> {
                Mono<Set<String>> followeesIdsMono = userService.findFolloweesByUsername(filter.getUsername());

                return followeesIdsMono.flatMapMany(followeesIds -> {
                    return repository.findPosts(followeesIds, filter.getOffset(), filter.getLimit())
                            .switchIfEmpty(Mono.error(new NotFoundException("Nenhuma publicação encontrada")));
                })
                        .doFirst(() -> {
                            System.out.println("# Buscando posts para o feed de " + filter.getUsername());
                        })
                        .doOnNext(post -> {
                            System.out.println("Post encontrado id: " + post.getId());
                        });
            });
        };
    }
}
