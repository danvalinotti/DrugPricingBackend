package com.galaxe.drugpriceapi.web.nap.postgresMigration.models.listeners;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.PriceRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Price;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

public class PriceListener {
    @Autowired
    PriceRepository priceRepository;

    @PrePersist
    public void userPrePersist(Price price) {
        Price pr = new Price();
        try {
            pr = priceRepository.findAll().get(0);

        }catch(Exception ex){
        }

        Double p = priceRepository.findLastPrice(price.getDrugDetailsId(),price.getProgramId()).get(0).getPrice();

    }
    @PreUpdate
    public void userPreUpdate(Price price) {
    }

    @PreRemove
    public void userPreRemove(Price price) {
    }
}
