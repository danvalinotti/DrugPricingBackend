package com.galaxe.drugpriceapi.web.nap.controller;

import com.galaxe.drugpriceapi.web.nap.blinkhealth.*;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugRequest;
import com.google.gson.Gson;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.CollectionUtils;

import java.util.concurrent.CompletableFuture;

@Component
public class BlinkClient {

    private Gson gson = new Gson();

    private Results results = new Results();

    private Price price = new Price();

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Results> getBlinkPharmacy(RequestObject requestObject) {
        String url = constructBlinkPharmacyURL(requestObject.getZipcode()).intern();
        WebClient webClient = WebClient.create(url);
        String str = webClient
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class).block();
        BlinkPharmacy pharmacy = gson.fromJson(str, BlinkPharmacy.class);
        for (Results r : pharmacy.getResult().getResults()) {
            if (r.getIs_supersaver().equalsIgnoreCase("true")) {
                return CompletableFuture.completedFuture(r);
            }
        }
        return CompletableFuture.completedFuture(results);

    }


    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Price> getBlinkPrice(RequestObject requestObject) {
        String url = constructBlinkPriceURL(requestObject.getDrugName()).intern();
        String requestedDosage = requestObject.getDosageStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%]", "").trim().intern();
       String str = "";

        try {
            WebClient webClient = WebClient.create(url);
             str = webClient
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(String.class).block();
        }catch(Exception e){
            return null;
        }
        BlinkHealth blinkHealth = gson.fromJson(str, BlinkHealth.class);

        if(blinkHealth.getResult().getDrug().getFormatted_name().contains("Generic")){
            return null;
        }

        if (!CollectionUtils.isEmpty(blinkHealth.getResult().getDrug().getForms())) {
            for (Forms form :blinkHealth.getResult().getDrug().getForms()) {


            if (!CollectionUtils.isEmpty(form.getDosages())) {
                for (Dosages dosage : form.getDosages()) {
                    String blinkDosage = dosage.getDisplay_dosage().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%]", "").trim().intern();
                    if (blinkDosage.equalsIgnoreCase(requestedDosage)) {
                        for (Quantities q : dosage.getQuantities()) {
                            Double d = Double.parseDouble(String.valueOf(requestObject.getQuantity()));
                            if (q.getRaw_quantity().equalsIgnoreCase(String.valueOf(d))) {
                                Price p = q.getPrice();
                                p.setMedId(dosage.getMed_id());
                                return CompletableFuture.completedFuture(p);
                            }
                        }

                    }
                }
            }
            }
        }
        return null;

    }


    private String constructBlinkPharmacyURL(String zipcode) {
        return "https://www.blinkhealth.com/api/v2/pharmacies?limit=10&allow_out_of_network=false&zip_code=" + zipcode + "&c_app=rx&c_platform=web&c_timestamp=1557344265151";
    }

    private String constructBlinkPriceURL(String name) {

        String newName = name.replace(" ", "-");
        return "https://www.blinkhealth.com/api/v2/user/drugs/detail/" +newName + "?c_app=rx&c_platform=web&c_timestamp=1557342444013";
    }
}
