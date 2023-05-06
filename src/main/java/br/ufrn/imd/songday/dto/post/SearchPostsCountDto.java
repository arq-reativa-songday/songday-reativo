package br.ufrn.imd.songday.dto.post;

import java.util.Date;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SearchPostsCountDto {
    @NotNull
    private Date start;
    @NotNull
    private Date end;
    @NotNull
    private Set<String> followees;
}
