package com.galaxe.drugpriceapi.web.nap.controller;

import com.galaxe.drugpriceapi.model.Drug;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Price;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Results;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugMasterRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugRequestRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class APIClient3 {

    @Autowired
    private BlinkClient blinkClient;
    @Autowired
    DrugRequestRepository drugRequestRepository;

    @Autowired
    DrugMasterRepository drugMasterRepository;

    @Async
    public CompletableFuture<Blink> getBlinkPharmacyPrice(RequestObject requestObject) throws ExecutionException, InterruptedException {
        try {
            CompletableFuture<Results> pharmacy = blinkClient.getBlinkPharmacy(requestObject);

            CompletableFuture<Price> price = blinkClient.getBlinkPrice(requestObject);

            if(price == null){
                requestObject.setDrugName(requestObject.getDrugName().split("\\s")[0]);
                price = blinkClient.getBlinkPrice(requestObject);
            }
            //Wait until they are all done
            CompletableFuture.allOf(pharmacy, price).join();
            try {
                if(price.join().getLocal().getRaw_value()!="") {
                    int drugId = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0).getId();
                    if (drugRequestRepository.findByDrugIdAndProgramId(drugId, 5).size() == 0) {
                        DrugRequest drugRequest = new DrugRequest();
                        drugRequest.setZipcode(requestObject.getZipcode());
                        drugRequest.setDrugName(requestObject.getDrugName().replace(" ", "-"));
                        drugRequest.setProgramId(5);
                        drugRequest.setGsn(price.join().getMedId());
                        drugRequest.setDrugId(drugId);
                        drugRequestRepository.save(drugRequest);
                    } else {
                        DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId, 5).get(0);
                        drugRequest.setZipcode(requestObject.getZipcode());
                        drugRequest.setDrugName(requestObject.getDrugName().replace(" ", "-"));
                        drugRequest.setProgramId(5);
                        drugRequest.setDrugId(drugId);
                        drugRequestRepository.save(drugRequest);
                    }
                }
            } catch (Exception ex) {

            }
            if (pharmacy != null && price != null) {
                Blink blink = new Blink();
                blink.setPrice(price.get());
                blink.setResults(pharmacy.get());
                return CompletableFuture.completedFuture(blink);
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new Blink());
        }
        return CompletableFuture.completedFuture(new Blink());
    }
}
