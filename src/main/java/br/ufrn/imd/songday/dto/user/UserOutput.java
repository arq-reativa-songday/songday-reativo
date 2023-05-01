package br.ufrn.imd.songday.dto.user;

import java.util.Date;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOutput {
    private String id;
    private String username;
    private String name;
    private Date createdAt;
    private Set<String> followees;
}
