package br.ufrn.imd.songday.dto.user;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import br.ufrn.imd.songday.model.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toUser(UserInput userInput);
    UserOutput toUserOutput(User user);
}
