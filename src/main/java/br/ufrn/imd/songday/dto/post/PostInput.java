package br.ufrn.imd.songday.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostInput {
    @NotBlank
    private String songId;

    private String text;
}
