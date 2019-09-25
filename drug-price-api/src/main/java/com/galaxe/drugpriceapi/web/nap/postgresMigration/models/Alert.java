package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.*;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@EntityListeners(AlertListener.class)
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
    String alertTypeId;

    @Column
    String detailedMessage;
    @Column
    String status;

    @Temporal(TemporalType.TIMESTAMP)
    Date time;


}
