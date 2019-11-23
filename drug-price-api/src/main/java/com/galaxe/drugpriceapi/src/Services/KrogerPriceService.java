package com.galaxe.drugpriceapi.src.Services;

public class KrogerPriceService {
    private static String[] kroger_names = new String[]{
            "KROGER AFFILIATES",
            "BAKER'S",
            "CITY MARKET",
            "COPPS FOOD CENTER",
            "DILLONS",
            "FOOD4LESS",
            "FRED MEYER",
            "FRY'S",
            "GENE MADDY",
            "GERBES PHARMACY",
            "HARRIS TEETER",
            "JAY C",
            "KING SOOPERS",
            "KROGER",
            "MARIANO'S",
            "METRO MARKET",
            "OWEN'S",
            "PAYLESS",
            "PICK 'N SAVE",
            "QFC",
            "RALPHS",
            "SCOTT'S",
            "SMITH'S",
    };

    static boolean isKroger(String pharmacyName) {
        for (String krogerPharmacy : kroger_names) {
            if (pharmacyName.contains(krogerPharmacy) || krogerPharmacy.contains(pharmacyName)) {
                return true;
            }
        }

        return false;
    }
}
