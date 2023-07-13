package br.ufrn.imd.songday.service;

import java.util.Set;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import br.ufrn.imd.songday.dto.post.GenerateFeedDto;
import br.ufrn.imd.songday.dto.post.PostSearchDto;
import br.ufrn.imd.songday.repository.PostRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class PostFunction {
    @Autowired
    private PostRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private StreamBridge streamBridge;

    @Bean
    public Function<Flux<GenerateFeedDto>, Flux<PostSearchDto>> retrievePosts() {
        return input -> {
            return input.flatMap(filter -> {
                Mono<Set<String>> followeesIdsMono = userService.findFolloweesByUsername(filter.getUsername())
                        .onErrorResume(error -> {
                            Message<String> event = MessageBuilder.withPayload(error.getMessage()).build();
                            streamBridge.send("errorsfeed", event);
                            return Mono.empty();
                        });

                return followeesIdsMono.flatMapMany(followeesIds -> {
                    return repository.findPosts(followeesIds, filter.getOffset(), filter.getLimit())
                            .switchIfEmpty(Mono.defer(() -> {
                                Message<String> event = MessageBuilder
                                        .withPayload("Nenhuma publicação encontrada para " + filter.getUsername())
                                        .build();
                                streamBridge.send("errorsfeed", event);
                                return Mono.empty();
                            }));
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
