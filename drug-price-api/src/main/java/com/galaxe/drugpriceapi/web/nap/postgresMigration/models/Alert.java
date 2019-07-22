package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.*;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    @Id
    @GeneratedValue
    int id;
    @Column
    String name;
    @Column
    String type;
    @Column
    String message;
    @Temporal(TemporalType.TIMESTAMP)
    Date time;

}
