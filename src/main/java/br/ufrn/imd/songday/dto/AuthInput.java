package br.ufrn.imd.songday.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AuthInput {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
