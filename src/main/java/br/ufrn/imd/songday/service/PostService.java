package br.ufrn.imd.songday.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.songday.dto.post.PostSearchDto;
import br.ufrn.imd.songday.dto.post.SearchPostsDto;
import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.exception.ValidationException;
import br.ufrn.imd.songday.model.Post;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.PostRepository;
import br.ufrn.imd.songday.util.DateUtil;

@Service
public class PostService {
    @Autowired
    private PostRepository repository;

    @Autowired
    private UserService userService;

    public Post createPost(Post newPost) {
        // lança exceção se o usuário não existir
        User user = userService.findById(newPost.getUserId());

        // TODO: verificar se a música existe

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

        // lança exceção se o usuário não existir
        User user = userService.findById(userId);

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
}
