package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.GoodRxResponse.GoodRxResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
@Component
public class GoodRxService {

    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;

    private Gson gson = new Gson();

    public CompletableFuture<GoodRxResponse> getGoodRxPrices(UIRequestObject UIRequestObject) {
        int drugId = 0;
        String str = "";
        DrugRequest drugRequest;
        List<DrugRequest> drugRequests;
        List<DrugMaster> drugMasters;
        if (UIRequestObject.getDrugName().toUpperCase().equals("ATORVASTATIN CALCIUM")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("CIPROFLOXACIN HCL")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("CIPROFLOXACIN HYDROCHLORIDE")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("CITALOPRAM HYDROBROMIDE")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("CLOMIPHENE CITRATE")) {
            System.out.println("ACYCLOVIR");
        }
        if (UIRequestObject.getDrugName().toUpperCase().equals("ERGOCALCIFEROL")) {
            System.out.println("ACYCLOVIR");
        }

        try {
            drugMasters = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode());
            drugId = drugMasters.get(0).getId();
            if (drugId == 265659) {
                System.out.println("ACYCLOVIR");
            }
            drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 6);
            drugRequest = drugRequests.get(0);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugRequest.getDrugId(), 6).size() != 0) {

                WebClient webClient = WebClient.create("https://www.goodrx.com/api/v4/drugs/" + drugRequest.getGood_rx_id() + "/prices?location=" + drugRequest.getLatitude() + "," + drugRequest.getLongitude() + "&location_type=LAT_LNG&distance_mi=6&quantity=" + drugRequest.getQuantity() + "");
                str = webClient
                        .get()
                        .retrieve().bodyToMono(String.class).block();
                GoodRxResponse goodRxResponse = gson.fromJson(str, GoodRxResponse.class);
                try {
                    if (goodRxResponse.getResults().get(0).getPrices().get(0).getPrice() == 0.0) {
                        System.out.println("");
                    }
                } catch (Exception ex) {
                    System.out.println("Caught");
                }
                return CompletableFuture.completedFuture(goodRxResponse);

            } else {
                return CompletableFuture.completedFuture(new GoodRxResponse());
            }
        } catch (Exception ex) {

        }

        return CompletableFuture.completedFuture(new GoodRxResponse());
    }
}
