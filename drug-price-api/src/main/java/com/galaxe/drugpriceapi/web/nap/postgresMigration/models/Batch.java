package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Batch {
    @Id
    @GeneratedValue
    int id;
    @Column
    int drugMasterId;
    @Column
    int reportId;
}
