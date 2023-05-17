package br.ufrn.imd.songday.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import feign.FeignException;
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
        // Mono<User> user = userReadOnlyRepository.findById(newPost.getUserId())
        //         .switchIfEmpty(Mono.error(new NotFoundException("Usuário não encontrado")));

        // if (!existsSongById(newPost.getSongId())) {
        //     throw new NotFoundException("Música não encontrada");
        // }

        // boolean hasPostToday = repository.existsByUserIdAndCreatedAtBetween(user.getId(), DateUtil.getTodayStartDate(),
        //         DateUtil.getTodayEndDate());
        // if (hasPostToday) {
        //     throw new ValidationException("Só é possível escolher uma música por dia");
        // }

        // Post postSaved = repository.save(newPost);
        // updateSongScore(postSaved.getSongId());

        // return postSaved;
        return Mono.empty();
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

        return post.flatMap(postFound -> {
            return userReadOnlyRepository.findById(userId).flatMap(
                    userFound -> {
                        boolean hasIdUser = postFound.getUserLikes().contains(userFound.getId());
                        if (hasIdUser) {
                            return Mono.error(
                                    new ValidationException("Não é possível curtir uma publicação mais de uma vez"));
                        }

                        postFound.getUserLikes().add(userFound.getId());
                        return repository.save(postFound);
                    }).switchIfEmpty(Mono.error(new NotFoundException("Usuário não encontrado")));
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

    private Boolean existsSongById(String songId) {
        try {
            ResponseEntity<Object> response = songsClient.findById(songId);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return false;
            }
            return response.getBody() != null ? true : false;
        } catch (FeignException e) {
            if (e.status() == 404) {
                return false;
            } else {
                e.printStackTrace();
                throw new ServicesCommunicationException(
                        "Erro durante a comunicação com Songs para recuperar a música por id");
            }
        }
    }

    private void updateSongScore(String songId) {
        try {
            ResponseEntity<Void> response = songsClient.updateScore(songId);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServicesCommunicationException(
                    "Erro durante a comunicação com Songs para atualizar o score da música");
            }
        } catch (FeignException e) {
            e.printStackTrace();
            throw new ServicesCommunicationException(
                    "Erro durante a comunicação com Songs para atualizar o score da música");
        }
    }
}
