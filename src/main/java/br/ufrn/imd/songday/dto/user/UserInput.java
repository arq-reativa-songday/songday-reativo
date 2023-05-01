package br.ufrn.imd.songday.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserInput {
    @Size(min = 3, max = 30)
    @NotBlank
    private String username;

    @NotBlank
    private String name;

    @NotBlank
    private String password;
}
