package br.ufrn.imd.songday.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.exception.ValidationException;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User newUser) {
        Optional<User> search = repository.findByUsername(newUser.getUsername());

        if (search.isPresent()) {
            throw new ValidationException("Nome de usuário já existe");
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        return repository.save(newUser);
    }

    public Page<User> findAll(Pageable pageable) {
        Page<User> userPage = repository.findAll(pageable);

        if (!userPage.hasContent()) {
            throw new NotFoundException("Nenhum usuário encontrado");
        }

        return userPage;
    }

    public User findById(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }
}
