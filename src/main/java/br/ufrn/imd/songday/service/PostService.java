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

@Service
public class PostService {
    @Autowired
    private PostRepository repository;

    @Autowired
    private SongsClient songsClient;

    @Autowired
    private UserReadOnlyRepository userReadOnlyRepository;

    public Post createPost(Post newPost) {
        User user = userReadOnlyRepository.findById(newPost.getUserId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (!existsSongById(newPost.getSongId())) {
            throw new NotFoundException("Música não encontrada");
        }

        boolean hasPostToday = repository.existsByUserIdAndCreatedAtBetween(user.getId(), DateUtil.getTodayStartDate(),
                DateUtil.getTodayEndDate());
        if (hasPostToday) {
            throw new ValidationException("Só é possível escolher uma música por dia");
        }

        return repository.save(newPost);
    }

    public List<PostSearchDto> findAll(SearchPostsDto search) {
        List<PostSearchDto> posts = repository.findPosts(search.getFollowees(), search.getOffset(), search.getLimit());

        if (posts.isEmpty()) {
            throw new NotFoundException("Nehuma publicação encontrada");
        }

        return posts;
    }

    public Post findById(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
    }

    public Post like(String idPost, String userId) {
        Post post = findById(idPost);

        User user = userReadOnlyRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        boolean hasIdUser = post.getUserLikes().contains(user.getId());
        if (hasIdUser) {
            throw new ValidationException("Não é possível curtir uma publicação mais de uma vez");
        }

        post.getUserLikes().add(user.getId());
        return repository.save(post);
    }

    public Post unlike(String idPost, String userId) {
        Post post = findById(idPost);

        boolean hasIdUser = post.getUserLikes().contains(userId);
        if (!hasIdUser) {
            throw new ValidationException("Publicação não curtida");
        }

        post.getUserLikes().remove(userId);
        return repository.save(post);
    }

    public int searchPostsCount(SearchPostsCountDto search) {
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
}
