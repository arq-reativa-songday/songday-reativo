package br.ufrn.imd.songday.dto.post;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import br.ufrn.imd.songday.model.Post;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {
    Post toPost(PostInput postInput);
}
