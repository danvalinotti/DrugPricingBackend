package com.galaxe.drugpriceapi.web.nap.mapper;

import com.galaxe.drugpriceapi.model.DrugNAP;
import com.galaxe.drugpriceapi.web.nap.model.DrugPrice;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;


@Mapper(componentModel = "spring")
@DecoratedWith(NAPMapperDecorator.class)
public interface NAPMapper {

    @Mappings({
            @Mapping(source = "as_of_date", target = "asOfDate"),
            @Mapping(source = "effective_date", target = "effectiveDate"),
            @Mapping(source = "ndc", target="ndc"),
            @Mapping(source = "ndc_description", target="name"),
            @Mapping(source = "pharmacy_type_indicator", target="pharmacyType"),
            @Mapping(source = "pricing_unit", target="pricingUnit"),
            @Mapping(source = "nadac_per_unit", target="nationalAverage"),
    })
    DrugNAP convertToMongoModel(DrugPrice drugPrice);
}
