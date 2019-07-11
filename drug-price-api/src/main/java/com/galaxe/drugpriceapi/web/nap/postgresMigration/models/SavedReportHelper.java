package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SavedReportHelper {

    int userId;

    String name;

    List<DrugMaster> drug_ids;

    List<String> drug_fields;


    List<String> providers;
}
