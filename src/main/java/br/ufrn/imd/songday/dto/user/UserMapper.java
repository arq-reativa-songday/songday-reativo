package br.ufrn.imd.songday.dto.user;

import org.mapstruct.Mapper;

import br.ufrn.imd.songday.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserInput userInput);
    UserOutput toUserOutput(User user);
}
