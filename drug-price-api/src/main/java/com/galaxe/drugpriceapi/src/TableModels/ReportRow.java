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

    public String get(Integer i){
        switch (i){
            case 0:
                return format(this.name);
            case 1:
                return format(this.rank);
            case 2:
                return format(this.ndc);
            case 3:
                return format(this.gsn);
            case 4:
                return format(this.dosage_strength);
            case 5:
                return format(this.quantity);
            case 6:
                return format(this.zip_code);
            case 7:
                return format(this.insiderx_price);
            case 8:
                return format(this.unc_price);
            case 9:
                return format(this.insiderx_pharmacy);
            case 10:
                return format(this.goodrx_price);
            case 11:
                return format(this.goodrx_pharmacy);
            case 12:
                return format(this.pharm_price);
            case 13:
                return format(this.pharm_pharmacy);
            case 14:
                return format(this.wellrx_price);
            case 15:
                return format(this.wellrx_pharmacy);
            case 16:
                return format(this.medimpact_price);
            case 17:
                return format(this.medimpact_pharmacy);
            case 18:
                return format(this.singlecare_price);
            case 19:
                return format(this.singlecare_pharmacy);
            case 20:
                return format(this.blink_price);
            case 21:
                return format(this.blink_pharmacy);
            case 22:
                return format(this.recommended_price);
            case 23:
                return getDifference();
            default:
                return "N/A";
//            case 24:




        }
    }

    private String getDifference() {
        try {
            Double difference;
            difference = Double.parseDouble(this.recommended_price) - Double.parseDouble(this.insiderx_price);
            return difference.toString();
        }catch (Exception ex){
            return "N/A";
        }
    }

    public String format(String s){
        if(s == null){
            return "N/A";
        }else{
            return s;
        }
    }

}
