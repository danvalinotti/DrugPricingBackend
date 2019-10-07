package com.galaxe.drugpriceapi.src.TableModels;

import com.galaxe.drugpriceapi.src.TableModels.listeners.AlertListener;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@EntityListeners(AlertListener.class)
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alert {//USED
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
