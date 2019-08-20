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
    Integer id ;
    String dosage_strength;
    String quantity;
    String name ;
    String insiderx_price ;
    String medimpact_price ;
    String wellrx_price ;
    String blink_price ;
    String singlecare_price ;
    String pharm_price ;


}
