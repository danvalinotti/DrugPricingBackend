package com.galaxe.drugpriceapi.web.nap.mapper;

import com.galaxe.drugpriceapi.model.DrugNAP;
import com.galaxe.drugpriceapi.repositories.DrugNAPRepository;
import com.galaxe.drugpriceapi.web.nap.model.DrugPrice;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class NAPMapperDecorator implements NAPMapper {

    @Autowired
    NAPMapper delegate;

    @Autowired
    DrugNAPRepository drugNAPRepository;

    @Override
    public DrugNAP convertToMongoModel(DrugPrice drugPrice) {
        DrugNAP nap = delegate.convertToMongoModel(drugPrice);
        return nap;
    }
}
