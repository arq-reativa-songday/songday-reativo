package br.ufrn.imd.songday.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.songday.client.SongsClient;
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
import br.ufrn.imd.songday.util.DateUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PostService {
    @Autowired
    private PostRepository repository;

    @Autowired
    private SongsClient songsClient;

    @Autowired
    private UserReadOnlyRepository userReadOnlyRepository;

    public Mono<Post> createPost(Post newPost) {
        Mono<User> user = userReadOnlyRepository.findById(newPost.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("Usuário não encontrado")));

        Mono<Boolean> existsSong = existsSongById(newPost.getSongId());

        Mono<Boolean> hasPostToday = repository
                .existsByUserIdAndCreatedAtBetween(newPost.getUserId(), DateUtil.getTodayStartDate(),
                        DateUtil.getTodayEndDate())
                .flatMap(hasPost -> {
                    return !hasPost ? Mono.just(hasPost)
                            : Mono.error(new ValidationException("Só é possível escolher uma música por dia"));
                });

        return Mono.zip(user, existsSong, hasPostToday)
                .flatMap(t -> repository.save(newPost))
                .doOnNext(post -> {
                    updateSongScore(post.getSongId()).subscribe();
                });
    }

    public Flux<PostSearchDto> findAll(SearchPostsDto search) {
        return repository.findPosts(search.getFollowees(), search.getOffset(), search.getLimit())
                .switchIfEmpty(Mono.error(new NotFoundException("Nehuma publicação encontrada")));
    }

    public Mono<Post> findById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Publicação não encontrada")));
    }

    public Mono<Post> like(String idPost, String userId) {
        Mono<Post> post = findById(idPost);
        Mono<User> user = userReadOnlyRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuário não encontrado")));

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

    public Mono<Post> unlike(String idPost, String userId) {
        Mono<Post> post = findById(idPost);

        return post.flatMap(postFound -> {
            boolean hasIdUser = postFound.getUserLikes().contains(userId);
            if (!hasIdUser) {
                return Mono.error(new ValidationException("Publicação não curtida"));
            }

            postFound.getUserLikes().remove(userId);
            return repository.save(postFound);
        });
    }

    public Mono<Long> searchPostsCount(SearchPostsCountDto search) {
        return repository.countByUserIdInAndCreatedAtBetween(search.getFollowees(), search.getStart(), search.getEnd());
    }

    private Mono<Boolean> existsSongById(String songId) {
        return songsClient.existsById(songId)
                .doOnError(e -> {
                    throw new ServicesCommunicationException(
                            "Erro durante a comunicação com Songs para recuperar a música por id: "
                                    + e.getLocalizedMessage());
                })
                .next()
                .flatMap(result -> {
                    return result ? Mono.just(result) : Mono.error(new NotFoundException("Música não encontrada"));
                });
    }

    private Mono<Void> updateSongScore(String songId) {
        return songsClient.updateScore(songId)
                .doOnError(e -> {
                    throw new ServicesCommunicationException(
                            "Erro durante a comunicação com Songs para atualizar o score da música: "
                                    + e.getLocalizedMessage());
                });
    }
}
