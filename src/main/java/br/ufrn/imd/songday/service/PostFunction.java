package br.ufrn.imd.songday.service;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import br.ufrn.imd.songday.dto.post.GenerateFeedDto;
import br.ufrn.imd.songday.dto.post.PostInput;
import br.ufrn.imd.songday.dto.post.PostMapper;
import br.ufrn.imd.songday.dto.post.PostSearchDto;
import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.exception.ValidationException;
import br.ufrn.imd.songday.model.Post;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.PostRepository;
import br.ufrn.imd.songday.repository.UserReadOnlyRepository;
import br.ufrn.imd.songday.util.DateUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class PostFunction {
    @Autowired
    private PostRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserReadOnlyRepository userReadOnlyRepository;

    @Autowired
    private PostMapper mapper;

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

    /**
     * Inicia a criação de um post, nesse método é validado se o usuário existe e se
     * ele ainda não fez nenhum post no dia.
     * 
     * @return post que deve ter a existência da música verificada
     */
    @Bean
    public Function<Flux<PostInput>, Flux<Post>> createPost() {
        return inputs -> {
            return inputs.map(mapper::toPost)
                    .doOnNext(post -> System.out.println("[POSTS] Validar post do usuário " + post.getUserId() + "..."))
                    .flatMap(newPost -> {
                        Mono<User> user = userReadOnlyRepository.findById(newPost.getUserId())
                                .switchIfEmpty(Mono.error(new NotFoundException(
                                        "Usuário com id " + newPost.getUserId() + " não encontrado")));

                        return Mono.zip(user, hasPostToday(newPost.getUserId()))
                                .flatMap(t -> Mono.just(newPost))
                                .onErrorResume(error -> {
                                    Message<String> event = MessageBuilder.withPayload(error.getMessage()).build();
                                    streamBridge.send("errorssongday", event);
                                    return Mono.empty();
                                });
                    });
        };
    }

    private Mono<Boolean> hasPostToday(String userId) {
        return repository.existsByUserIdAndCreatedAtBetween(userId,
                DateUtil.getTodayStartDate(),
                DateUtil.getTodayEndDate())
                .flatMap(hasPost -> {
                    return !hasPost ? Mono.just(hasPost)
                            : Mono.error(new ValidationException(
                                    "Só é possível escolher uma música por dia. userId=" + userId));
                });
    }

    /**
     * Salva um post no banco de dados.
     * Esse método deve ser executado após confirmar que a música existe.
     * 
     * @return post salvo
     */
    @Bean
    public Function<Flux<Post>, Flux<Post>> postsSongExists() {
        return posts -> {
            return posts
                    .doOnNext(post -> System.out.println("[POSTS] Post do usuário " + post.getUserId() + " validado"))
                    .flatMap(repository::save);
        };
    }

    @Bean
    public Consumer<Flux<Post>> printCreatedPost() {
        return posts -> {
            posts.doOnNext(post -> {
                System.out.println("[POSTS] Novo post criado por " + post.getUserId());
            }).subscribe();
        };
    }

    @Bean
    public Consumer<Flux<String>> printErrorsSongDay() {
        return errors -> {
            errors.doOnNext(error -> {
                System.out.println("[ERRORS] " + error);
            }).subscribe();
        };
    }
}
