package br.ufrn.imd.songday.dto.post;

import lombok.Getter;

@Getter
public class GenerateFeedDto {
    private String username;
    private int offset;
    private int limit;
}
