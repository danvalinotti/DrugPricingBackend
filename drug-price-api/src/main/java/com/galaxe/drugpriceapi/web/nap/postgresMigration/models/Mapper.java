package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.PriceRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.Date;


public class Mapper {


    @Autowired
    PriceRepository priceRepository;

    public Price getLastPrice(Price price){
        return priceRepository.findLastPrice(price.drugDetailsId, price.getProgramId()).get(0);
    }
}
