package com.galaxe.drugpriceapi.web.nap.controller;

import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Price;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Results;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class APIClient3 {

    @Autowired
    private BlinkClient blinkClient;

    @Async
    public CompletableFuture<Blink> getBlinkPharmacyPrice(RequestObject requestObject) throws ExecutionException, InterruptedException {
       try {
           CompletableFuture<Results> pharmacy = blinkClient.getBlinkPharmacy(requestObject);

        CompletableFuture<Price> price = blinkClient.getBlinkPrice(requestObject);

        //Wait until they are all done
        CompletableFuture.allOf(pharmacy, price).join();

        if (pharmacy != null && price != null) {
            Blink blink = new Blink();
            blink.setPrice(price.get());
            blink.setResults(pharmacy.get());
            return CompletableFuture.completedFuture(blink);
        }
       }catch(Exception e ){
           return CompletableFuture.completedFuture(new Blink());
       }
        return CompletableFuture.completedFuture(new Blink());
    }
}
