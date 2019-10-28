package com.galaxe.drugpriceapi.src.TableModels;

import com.galaxe.drugpriceapi.src.Helpers.IntArrayUserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@Table(name = "trailing_drugs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(
        name = "int-array",
        typeClass = IntArrayUserType.class
)
public class TrailingDrugs {
    @Id
    @GeneratedValue
    private int id;

    @Column(name = "drug_id")
    private Integer drugId;

    @Type(type ="int-array")
    @Column(name = "price_ids", columnDefinition = "integer[]")
    private Integer[] priceIds;

    @Column(name = "report_id")
    private Integer reportId;
}