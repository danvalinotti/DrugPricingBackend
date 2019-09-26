package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReportRow {
    @Id
    Integer id;
    String gsn;
    String ndc;
    String zip_code;
    String dosage_strength;
    String quantity;
    String name ;
    String drug_id;
    String insiderx_price ;
    String insiderx_pharmacy ;
    String goodrx_price ;
    String goodrx_pharmacy  ;
    String medimpact_price ;
    String medimpact_pharmacy ;
    String wellrx_price ;
    String wellrx_pharmacy  ;
    String blink_price ;
    String blink_pharmacy  ;
    String singlecare_price ;
    String singlecare_pharmacy  ;
    String pharm_price ;
    String pharm_pharmacy  ;
    String recommended_price;
    String rank;

}
