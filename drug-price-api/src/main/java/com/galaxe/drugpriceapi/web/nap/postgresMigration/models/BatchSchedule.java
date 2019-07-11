package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchSchedule {
    @Id
    @GeneratedValue
    int id;
    @Column
    int batchId;
    @Column
    Date createdAt;


}
