package br.ufrn.imd.songday.dto.comment;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentOutput {
    private String id;
    private String postId;
    private String userId;
    private String text;
    private Date createdAt;
}
