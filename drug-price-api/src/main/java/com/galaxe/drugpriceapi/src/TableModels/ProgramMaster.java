package com.galaxe.drugpriceapi.src.TableModels;

import lombok.*;

import javax.persistence.*;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramMaster {
    @Id
    @GeneratedValue
    int id;
    @Column
    String name;
    @Column
    boolean isactive;
}
