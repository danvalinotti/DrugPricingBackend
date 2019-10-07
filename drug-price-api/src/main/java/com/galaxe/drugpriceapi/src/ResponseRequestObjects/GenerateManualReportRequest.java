package com.galaxe.drugpriceapi.src.ResponseRequestObjects;

import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateManualReportRequest {

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
