package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.DrugRequestRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxRequest.InsideRxRequest;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxResponse.InsideRxPrice;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxResponse.InsideRxResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
@Component
public class InsideRxService {

    private final String CSRF_TOKEN = "Hi6yGXfg-vppErZsd2KXvKmH9LxjPBNJeK48";
    private final String COOKIE = "_gcl_au=1.1.1639244140.1555443999; _fbp=fb.1.1555443999440.65320427; _ga=GA1.2.711100002.1555444000; _gid=GA1.2.317294123.1555444000; _hjIncludedInSample=1; _csrf=Z3iefjYKIjIUIEXBJgTix0BY; _gat_UA-113293481-1=1; geocoords=40.7473758%2C-74.05057520000003; AWSALB=6NBPPYHYpRwHG5ONO7yvFP6fzmSCfiDRLUr3FCKprscG4ld2CKg2lU+ZRCxhxrTF55clcMF7APSLyeZBhLeH2pv/9pzCIWt8u9lcfJfF8La8Z/eIpABRoF3orpJj";
    private Comparator<InsideRxPrice> insideRxComparator = null;
    private final String INSIDERXURL = "https://insiderx.com/request/pharmacies";
    private Gson gson = new Gson();

    @Autowired
    DrugRequestRepository drugRequestRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<InsideRxResponse>> constructInsideRxWebClient(UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude) throws ExecutionException, InterruptedException {

        InsideRxRequest insideRxRequest = new InsideRxRequest();
        int drugId = 0;
        try {


            drugId = drugMasterRepository.findAllByFields(UIRequestObject.getDrugNDC(), UIRequestObject.getQuantity(), UIRequestObject.getZipcode()).get(0).getId();

            List<DrugRequest> drugRequests = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0);

            if (drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0).size() != 0) {

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0).get(0);
                if (drugRequest.getDrugName() == null || drugRequest.getDrugName().equals("")) {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                    drugRequestRepository.save(drugRequest);
                }
                if (drugRequest.getLatitude() == null) {
                    drugRequest.setLatitude(40.585624 + "");
                }

                insideRxRequest.setLatitude(drugRequest.getLatitude());
                insideRxRequest.setLongitude(drugRequest.getLongitude());
                insideRxRequest.setNdc(drugRequest.getNdc());
                insideRxRequest.setQuantity(drugRequest.getQuantity());
                insideRxRequest.setSite_identity("irx");

            } else {
                insideRxRequest = getPostObject(UIRequestObject, longitudeLatitude);
            }
        } catch (Exception ex) {
            insideRxRequest = getPostObject(UIRequestObject, longitudeLatitude);
        }


        WebClient webClient2 = WebClient.create(INSIDERXURL);
        List<InsideRxResponse> insideRxList = new ArrayList<>();
        try {

            if (drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0).size() == 0) {
                DrugRequest drugRequest = new DrugRequest();
                drugRequest.setProgramId(0);
                drugRequest.setDrugId(drugId + "");
                drugRequest.setBrandIndicator(UIRequestObject.getDrugType());
                drugRequest.setLongitude(insideRxRequest.getLongitude());
                drugRequest.setLatitude(insideRxRequest.getLatitude());
                drugRequest.setNdc(insideRxRequest.getNdc());
                drugRequest.setQuantity(insideRxRequest.getQuantity());
                try {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                } catch (Exception ex) {

                }
                drugRequestRepository.save(drugRequest);
            } else {

                DrugRequest drugRequest = drugRequestRepository.findByDrugIdAndProgramId(drugId + "", 0).get(0);
                drugRequest.setProgramId(0);
                drugRequest.setDrugId(drugId + "");
                drugRequest.setBrandIndicator(UIRequestObject.getDrugType());
                drugRequest.setLatitude(insideRxRequest.getLatitude());
                drugRequest.setLongitude(insideRxRequest.getLongitude());
                drugRequest.setNdc(insideRxRequest.getNdc());
                drugRequest.setQuantity(insideRxRequest.getQuantity());
                try {
                    drugRequest.setDrugName(UIRequestObject.getDrugName());
                } catch (Exception ex) {

                }
                drugRequestRepository.save(drugRequest);
            }
        } catch (Exception ex) {
        }
        try {
            insideRxList = webClient2
                    .post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("csrf-token", CSRF_TOKEN)
                    .header("Cookie", COOKIE)
                    .body(Mono.just(insideRxRequest), InsideRxRequest.class)
                    .exchange()
                    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(InsideRxResponse.class))
                    .collectList().block();
        } catch (Exception ex) {
        }


        if (!CollectionUtils.isEmpty(insideRxList)) {
            if (!CollectionUtils.isEmpty(insideRxList.get(0).getPrices())) {
                if (insideRxComparator == null)
                    insideRxComparator = constructInsideRxComparator();

                Collections.sort(insideRxList.get(0).getPrices(), insideRxComparator);
            }
        }

        return CompletableFuture.completedFuture(insideRxList);
    }

    private InsideRxRequest getPostObject(UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude) {
        InsideRxRequest p = new InsideRxRequest();
        p.setLatitude(longitudeLatitude.get("latitude"));
        p.setLongitude(longitudeLatitude.get("longitude"));
        p.setNdc(UIRequestObject.getDrugNDC());
        p.setQuantity(String.valueOf(UIRequestObject.getQuantity()));
        p.setSite_identity("irx");
        return p;
    }

    private Comparator<InsideRxPrice> constructInsideRxComparator() {
        return (o1, o2) -> {
            Double p1 = Double.parseDouble(o1.getPrice());
            Double p2 = Double.parseDouble(o2.getPrice());
            p1.compareTo(p2);
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
