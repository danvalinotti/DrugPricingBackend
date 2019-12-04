package com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SortComparator;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceDetails implements Comparable {

    String program;

    String pharmacy;

    String price;

    String uncPrice;

    Boolean uncPriceFlag;

    String diff;

    String diffPerc;

    String rank;

    @Override
    public int compareTo(Object p2) {
        if (p2 instanceof PriceDetails && ((PriceDetails) p2).getPrice() != null) {
            return (int) Math.round(parseDouble(this.getPrice()) - parseDouble(((PriceDetails) p2).getPrice()));
        }

        return -1;
    }
}
