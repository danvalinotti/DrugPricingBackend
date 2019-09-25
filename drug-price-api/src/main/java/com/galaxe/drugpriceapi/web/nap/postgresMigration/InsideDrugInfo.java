package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class InsideDrugInfo {
    List<DrugDosage> drugDosageList;
}
