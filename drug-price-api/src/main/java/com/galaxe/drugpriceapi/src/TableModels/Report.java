package com.galaxe.drugpriceapi.src.TableModels;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reportTable")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue
    int id;

    @Column
    Integer userId;
    @Temporal(TemporalType.TIMESTAMP)
    Date timestamp;
    @Column
    Integer drugCount;

}
