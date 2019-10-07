package com.galaxe.drugpriceapi.src.Helpers;

import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricesAndMaster {
    List<Price> prices ;
    DrugMaster drugMaster;

}
