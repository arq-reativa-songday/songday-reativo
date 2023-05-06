package br.ufrn.imd.songday.dto.post;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SearchPostsDto {
    private int offset = 0;
    @Min(1)
    private int limit = 20;
    @NotNull
    private List<String> followees;
}
