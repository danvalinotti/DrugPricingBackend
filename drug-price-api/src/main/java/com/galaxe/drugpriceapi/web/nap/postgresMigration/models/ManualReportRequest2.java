package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManualReportRequest2 {

    List<DrugMaster> drugs;
    List<String> drugDetails;
    List<String> providers;
    String name;
    boolean isSaved;
    String token;

    public boolean getIsSaved(){
        return this.isSaved;
    }
    public void setIsSaved(boolean saved){
        this.isSaved = saved;
    }
}
