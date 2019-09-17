package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Table
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SavedReportDetails {
    @Id
    @GeneratedValue
    int id;

    @Column
    int userId;

    @Column
    String name;

    @ElementCollection
    List<Integer> drug_ids;

    @ElementCollection
    List<String> drug_fields;

    @ElementCollection
    List<String> providers;
}
