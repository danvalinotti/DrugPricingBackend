package com.galaxe.drugpriceapi.web.nap.masterList;

import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Document(collection = "masterList")
@Getter
@Setter
@AllArgsConstructor
public class MasterList {

    @Id
    String id ;
    List<MongoEntity> drug;
    BatchDetails batchDetails;
    int totalBatches;

    public MasterList(){}

    public MongoEntity getDrugByNDC(String ndc, String quantity, String zipCode) {
        for (MongoEntity entity:this.drug) {
            if(entity.getNdc().equals(ndc) &&entity.getZipcode().equals(zipCode)
                    && entity.getQuantity().equals(quantity)){
                return entity;
            }
        }
        return null;
    }
}
