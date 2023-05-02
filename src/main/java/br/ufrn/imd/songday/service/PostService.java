package br.ufrn.imd.songday.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufrn.imd.songday.model.Post;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.PostRepository;

@Service
public class PostService {
    @Autowired
    private PostRepository repository;

    public Post createPost(Post newPost, User user) {
        // TODO: verificar se a música existe
        // TODO: verificar se o usuário já escolheu uma música no dia
        newPost.setUserId(user.getId());
        return repository.save(newPost);
    }
}
