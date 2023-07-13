package br.ufrn.imd.songday.service;

import br.ufrn.imd.songday.client.SongsClient;
import br.ufrn.imd.songday.client.SongsClientResilience;
import br.ufrn.imd.songday.dto.post.PostSearchDto;
import br.ufrn.imd.songday.dto.post.SearchPostsCountDto;
import br.ufrn.imd.songday.dto.post.SearchPostsDto;
import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.exception.ServicesCommunicationException;
import br.ufrn.imd.songday.exception.ValidationException;
import br.ufrn.imd.songday.model.Post;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.PostRepository;
import br.ufrn.imd.songday.repository.UserReadOnlyRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.bytebuddy.implementation.bytecode.Throw;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PostService {
    @Autowired
    private PostRepository repository;

    @Autowired
    private SongsClient songsClient;

    @Autowired
    private SongsClientResilience songsClientResilience;

    @Autowired
    private UserReadOnlyRepository userReadOnlyRepository;

    @Autowired
    private RedissonReactiveClient redisClient;

    public Mono<Post> createPost(Mono<Post> post) {
        return post.flatMap(newPost -> {
            Mono<User> user = userReadOnlyRepository.findById(newPost.getUserId())
                    .switchIfEmpty(Mono.error(new NotFoundException("Usuário não encontrado")));

            Mono<Boolean> existsSong = existsSongById(newPost.getSongId());

            Mono<Boolean> hasPostToday = Mono.just(false);
//            Mono<Boolean> hasPostToday = repository
//                    .existsByUserIdAndCreatedAtBetween(newPost.getUserId(), DateUtil.getTodayStartDate(),
//                            DateUtil.getTodayEndDate())
//                    .flatMap(hasPost -> {
//                        return !hasPost ? Mono.just(hasPost)
//                                : Mono.error(new ValidationException("Só é possível escolher uma música por dia"));
//                    });

            return Mono.zip(user, existsSong, hasPostToday)
                    .flatMap(t -> repository.save(newPost))
                    .doOnNext(postSaved -> {
                        updateSongScore(postSaved.getSongId());
                    });
        });
    }

    public Flux<PostSearchDto> findAll(Mono<SearchPostsDto> search) {
        return search.flatMapMany(s -> repository.findPosts(s.getFollowees(), s.getOffset(), s.getLimit())
                .switchIfEmpty(Mono.error(new NotFoundException("Nenhuma publicação encontrada"))));
    }

    public Mono<Post> findById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Publicação não encontrada")));
    }

    public Mono<Post> like(String idPost, Mono<String> userId) {
        Mono<Post> post = findById(idPost);
        Mono<User> user = userId.flatMap(id -> userReadOnlyRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuário não encontrado"))));

        return post.zipWith(user).flatMap(t -> {
            Post postFound = t.getT1();
            User userFound = t.getT2();
            boolean hasIdUser = postFound.getUserLikes().contains(userFound.getId());
            if (hasIdUser) {
                return Mono.error(new ValidationException("Não é possível curtir uma publicação mais de uma vez"));
            }

            postFound.getUserLikes().add(userFound.getId());
            return repository.save(postFound);
        });
    }

    public Mono<Post> unlike(String idPost, Mono<String> userId) {
        Mono<Post> post = findById(idPost);

        return post.zipWith(userId).flatMap(t -> {
            Post postFound = t.getT1();
            String id = t.getT2();

            boolean hasIdUser = postFound.getUserLikes().contains(id);
            if (!hasIdUser) {
                return Mono.error(new ValidationException("Publicação não curtida"));
            }

            postFound.getUserLikes().remove(id);
            return repository.save(postFound);
        });
    }

    public Mono<Long> searchPostsCount(Mono<SearchPostsCountDto> search) {
        return search.flatMap(s -> repository.countByUserIdInAndCreatedAtBetween(s.getFollowees(), s.getStart(), s.getEnd()));
    }

    private Mono<Boolean> existsSongById(String songId) {
        return songsClientResilience.findById(songId)
                .doOnError(e -> {
                    if (e.getLocalizedMessage().contains("404 Not Found")) {
                        throw new NotFoundException("Música não encontrada");
                    }
                    throw new ServicesCommunicationException(
                            "Erro durante a comunicação com Songs para recuperar a música por id: "
                                    + e.getLocalizedMessage());
                })
                .next()
                .flatMap(result -> Mono.just(Boolean.TRUE));
    }

    private void updateSongScore(String songId) {
        RTopicReactive songPopularityTopic = redisClient.getTopic("songPopularityReactiveTopic", StringCodec.INSTANCE);
        songPopularityTopic.publish(songId);

//        return songsClient.updateScore(songId)
//                .doOnError(e -> {
//                    throw new ServicesCommunicationException(
//                            "Erro durante a comunicação com Songs para atualizar o score da música: "
//                                    + e.getLocalizedMessage());
//                });
    }
}
