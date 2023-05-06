package br.ufrn.imd.songday.model;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String name;
    private String password;
    private Date createdAt = new Date();
    private Set<String> followees;

    public User() {
        followees = Collections.emptySet();
    }
}
