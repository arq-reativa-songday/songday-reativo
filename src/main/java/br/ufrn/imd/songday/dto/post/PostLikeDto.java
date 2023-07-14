package br.ufrn.imd.songday.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostLikeDto {
    @NotBlank
    private String id;

    @NotBlank
    private String userId;
}
