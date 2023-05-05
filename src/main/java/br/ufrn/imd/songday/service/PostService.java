package br.ufrn.imd.songday.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.songday.dto.post.PostFeedDto;
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

    public Post createPost(Post newPost, User user) {
        // TODO: verificar se a música existe

        boolean hasPostToday = repository.existsByUserIdAndCreatedAtBetween(user.getId(), DateUtil.getTodayStartDate(),
                DateUtil.getTodayEndDate());
        if (hasPostToday) {
            throw new ValidationException("Só é possível escolher uma música por dia");
        }

        newPost.setUserId(user.getId());
        return repository.save(newPost);
    }

    public List<PostFeedDto> findAll() {
        List<String> list = Arrays.asList("64541ceacc28ad7158967d78");
        return repository.findPosts(list, 0, 20);
    }

    public Post findById(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
    }

    public Post like(String idPost, User user) {
        Post post = findById(idPost);

        boolean hasIdUser = post.getUserLikes().contains(user.getId());
        if (hasIdUser) {
            throw new ValidationException("Você já curtiu essa publicação");
        }

        post.getUserLikes().add(user.getId());
        return repository.save(post);
    }

    public Post unlike(String idPost, User user) {
        Post post = findById(idPost);

        boolean hasIdUser = post.getUserLikes().contains(user.getId());
        if (!hasIdUser) {
            throw new ValidationException("Você não curtiu essa publicação");
        }

        post.getUserLikes().remove(user.getId());
        return repository.save(post);
    }
}
