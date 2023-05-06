package br.ufrn.imd.songday.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentInput {
    @NotBlank
    private String userId;

    @NotBlank
    private String text;
}
