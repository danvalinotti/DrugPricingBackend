package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Controllers.DrugMasterController;
import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.PriceRepository;
import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.client.RestTemplate;
import reactor.util.LinkedMultiValueMap;
import reactor.util.MultiValueMap;

import java.util.*;

import static com.galaxe.drugpriceapi.src.Services.BlinkClient.getBlinkPrices;
import static com.galaxe.drugpriceapi.src.Services.GoodRxService.getGoodRxPrices;
import static com.galaxe.drugpriceapi.src.Services.InsideRxService.getInsideRxPrice;
import static com.galaxe.drugpriceapi.src.Services.MedImpactService.getMedImpactPrices;
import static com.galaxe.drugpriceapi.src.Services.SinglecareService.getSingleCarePrices;
import static com.galaxe.drugpriceapi.src.Services.WellRxService.getWellRXPrices;
import static java.lang.Double.parseDouble;

public class RealTimeSearchService {
    @Autowired
    private static DrugMasterRepository drugMasterRepository;
    @Autowired
    private static PriceRepository priceRepository;
    @Autowired
    private static DrugMasterController drugMasterController;
    @Autowired
    private static BlinkClient blinkClient;
    @Autowired
    private static GoodRxService goodRxService;
    @Autowired
    private static InsideRxService insideRxService;
    @Autowired
    private static MedImpactService medImpactService;
    @Autowired
    private static SinglecareService singlecareService;
    @Autowired
    private static WellRxService wellRxService;
    @Autowired
    private static USPharmService usPharmService;

    public static ArrayList<Price> getCompetitorPrices(DrugRequest drugRequest, int programId) {
        try {
            Price price = new Price();
            price.setProgramId(programId);
            String brandType = drugRequest.getDrugType();

            // InsideRX
            switch (programId) {
                case 0:
                    return getInsideRxPrice(drugRequest);
                case 1:
                    return new ArrayList<>();
                case 2:
                    return getWellRXPrices(drugRequest);
                case 3:
                    return getMedImpactPrices(drugRequest);
                case 4:
                    return getSingleCarePrices(drugRequest);
                case 5:
                    return getBlinkPrices(drugRequest);
                case 6:
                    return getGoodRxPrices(drugRequest);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
