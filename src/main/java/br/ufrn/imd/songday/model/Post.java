package br.ufrn.imd.songday.model;

import java.util.Date;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "posts")
@Data
public class Post {
    @Id
    private String id;
    private String songId;
    private String userId;
    private String text;
    @Indexed(direction = IndexDirection.DESCENDING)
    private Date createdAt;
    private Set<String> userLikes;
}
