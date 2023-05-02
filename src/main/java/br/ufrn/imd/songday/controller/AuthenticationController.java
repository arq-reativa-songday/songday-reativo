package br.ufrn.imd.songday.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.ufrn.imd.songday.dto.AuthInput;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.security.JwtTokenUtil;
import jakarta.validation.Valid;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthInput authInput) throws JsonProcessingException {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authInput.getUsername(), authInput.getPassword()));
        String token = jwtService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok(token);
    }
}
