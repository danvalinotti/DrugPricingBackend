package com.galaxe.drugpriceapi.src.TableModels;

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
    public Integer id;
    public String gsn;
    public String ndc;
    public String zip_code;
    public String dosage_strength;
    public String quantity;
    public String name ;
    public String drug_id;
    public String insiderx_price ;
    public String insiderx_pharmacy ;
    public String goodrx_price ;
    public String goodrx_pharmacy  ;
    public String medimpact_price ;
    public String medimpact_pharmacy ;
    public String wellrx_price ;
    public String wellrx_pharmacy  ;
    public String blink_price ;
    public String blink_pharmacy  ;
    public String singlecare_price ;
    public String singlecare_pharmacy  ;
    public String pharm_price ;
    public String pharm_pharmacy  ;
    public String recommended_price;
    public String rank;
    public String unc_price;

}
