package br.ufrn.imd.songday.dto.post;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collation = "posts")
public class PostSearchDto {
    @Id
    private String id;

    private String songId;

    @Field(value = "user.username")
    private String username;

    private String text;

    private Date createdAt;

    @Field(value = "commentsCount")
    private int comments;

    @Field(value = "likesCount")
    private int likes;
}
