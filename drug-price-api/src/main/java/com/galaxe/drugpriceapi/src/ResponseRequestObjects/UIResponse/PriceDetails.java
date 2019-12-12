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

        try {
            if (!((PriceDetails) p2).getPrice().equals("null") && !(this.getPrice().equals("null"))) {
                return (int) Math.round(parseDouble(this.getPrice()) - parseDouble(((PriceDetails) p2).getPrice()));
            }
        } catch (Exception e) {
            System.out.println("Cannot compare prices as price is null.");
            return -1;
        }

        return -1;
    }
}
