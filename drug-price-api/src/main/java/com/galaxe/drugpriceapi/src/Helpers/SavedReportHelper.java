package com.galaxe.drugpriceapi.src.Helpers;

import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
