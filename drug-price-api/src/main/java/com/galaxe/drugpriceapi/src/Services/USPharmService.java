package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.USPharmResponse.USPharmPrice;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.USPharmResponse.USPharmResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.WellRxResponse.Drugs;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
@Component
public class USPharmService {
    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    DrugRequestRepository drugRequestRepository;
    private Gson gson = new Gson();
    private List<Drugs> drugs = new ArrayList<>();

    private Comparator<USPharmPrice> constructUsDrugComparator = null;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<USPharmResponse>> constructUsPharmacy(UIRequestObject UIRequestObject) {

        String drugType = UIRequestObject.getDrugType();
        if (drugType.equals("B")) {
            drugType = "BRAND_WITH_GENERIC";
        } else if (drugType.equals("G")) {
            drugType = "GENERIC";
        }

        WebClient webClient;
        int drugId = 0;
        try {
            drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();

            List<DrugRequest>drugRequests= drugRequestRepository.findByDrugIdAndProgramId(drugId+"",1);

            if(drugRequestRepository.findByDrugIdAndProgramId(drugId+"",1).size() != 0){

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"",1).get(0);

                webClient= WebClient.create("https://api.uspharmacycard.com/drug/price/147/020982/" + drugRequest.getZipcode()
                        + "/" + drugRequest.getNdc() + "/" + drugRequest.getDrugName() + "/" + drugRequest.getBrandIndicator() + "/" + drugRequest.getQuantity() + "/10");

            }else{
                webClient= WebClient.create("https://api.uspharmacycard.com/drug/price/147/020982/" + UIRequestObject.getZipcode()
                        + "/" + UIRequestObject.getDrugNDC() + "/" + UIRequestObject.getDrugName() + "/" + drugType + "/" + UIRequestObject.getQuantity() + "/10");

            }
        }catch (Exception ex){
            webClient= WebClient.create("https://api.uspharmacycard.com/drug/price/147/020982/" + UIRequestObject.getZipcode()
                    + "/" + UIRequestObject.getDrugNDC() + "/" + UIRequestObject.getDrugName() + "/" + drugType + "/" + UIRequestObject.getQuantity() + "/10");
        }


        List<USPharmResponse> usPharmacies = new ArrayList<>();

        try {
            usPharmacies = webClient
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(USPharmResponse.class))
                    .collectList().block();
            try {

                if (drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 1).size() == 0) {
                    DrugRequest drugRequest = new DrugRequest();
                    drugRequest.setProgramId(1);
                    drugRequest.setDrugId(drugId+"");
                    drugRequest.setZipcode(UIRequestObject.getZipcode());
                    drugRequest.setNdc(UIRequestObject.getDrugNDC());
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                    drugRequest.setBrandIndicator(drugType);
                    drugRequest.setQuantity(UIRequestObject.getQuantity() + "");
                    drugRequestRepository.save(drugRequest);
                }else{
                    DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId+"", 1).get(0);
                    drugRequest.setProgramId(1);
                    drugRequest.setDrugId(drugId+"");
                    drugRequest.setZipcode(UIRequestObject.getZipcode());
                    drugRequest.setNdc(UIRequestObject.getDrugNDC());
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                    drugRequest.setBrandIndicator(drugType);
                    drugRequest.setQuantity(UIRequestObject.getQuantity() + "");
                    drugRequestRepository.save(drugRequest);
                }
            } catch (Exception ex) {

            }
            if (!CollectionUtils.isEmpty(usPharmacies.get(0).getPriceList())) {
                List<USPharmPrice> priceList = usPharmacies.get(0).getPriceList();


                if (constructUsDrugComparator == null)
                    constructUsDrugComparator = constructUsDrugComparator();

                Collections.sort(priceList, constructUsDrugComparator);
            }
        } catch (Exception ex) {

        }

        return CompletableFuture.completedFuture(usPharmacies);
    }
    private Comparator<USPharmPrice> constructUsDrugComparator() {
        return (o1, o2) -> {
            Double p1 = Double.parseDouble(o1.getDiscountPrice());
            Double p2 = Double.parseDouble(o2.getDiscountPrice());

            if (p1 > p2) {
                return 1;
            } else if (p1 < p2) {
                return -1;
            } else {
                return 0;
            }
        };
    }
}
