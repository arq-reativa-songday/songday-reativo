package br.ufrn.imd.songday.dto.comment;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import br.ufrn.imd.songday.model.Comment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {
    Comment toComment(CommentInput commentInput);
    CommentOutput toCommentOutput(Comment comment);
}
