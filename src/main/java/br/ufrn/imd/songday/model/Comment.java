package br.ufrn.imd.songday.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "comments")
@Data
public class Comment {
    @Id
    private String id;
    private String postId;
    private String userId;
    private String text;
    private Date createdAt = new Date();
}
