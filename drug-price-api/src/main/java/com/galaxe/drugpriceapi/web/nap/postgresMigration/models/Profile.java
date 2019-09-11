package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.*;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@EntityListeners(ProfileListener.class)
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    @GeneratedValue
    int id;
    @Column
    String name;
    @Column
    String username;
    @Column
    String password;
    @Column
    String role;
    @Column
    String activeToken;
    @Temporal(TemporalType.TIMESTAMP)
    Date tokenDate;

}
