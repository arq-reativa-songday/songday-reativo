package br.ufrn.imd.songday.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
