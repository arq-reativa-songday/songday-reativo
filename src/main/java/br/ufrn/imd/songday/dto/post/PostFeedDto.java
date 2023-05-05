package br.ufrn.imd.songday.dto.post;

import java.util.Date;

import lombok.Data;
import nonapi.io.github.classgraph.json.Id;

@Data
public class PostFeedDto {
    @Id
    private String id;

    private String songId;

    // @Field("{'$arrayElemAt': ['$user', 0]}")
    private String username;

    private Object user;

    private String text;

    private Date createdAt;

    private int likesCount;
}
