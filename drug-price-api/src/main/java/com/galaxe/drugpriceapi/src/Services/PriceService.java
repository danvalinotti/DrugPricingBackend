package com.galaxe.drugpriceapi.src.Services;

import com.galaxe.drugpriceapi.src.Controllers.DrugMasterController;
import com.galaxe.drugpriceapi.src.Controllers.PriceController;
import com.galaxe.drugpriceapi.src.Helpers.DrugBrand;
import com.galaxe.drugpriceapi.src.Helpers.PricesAndMaster;
import com.galaxe.drugpriceapi.src.Repositories.DrugMasterRepository;
import com.galaxe.drugpriceapi.src.Repositories.PriceRepository;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.BlinkHealthResponse.BlinkResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.GoodRxResponse.GoodRxResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxResponse.InsideRxPrice;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.PriceDetails;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.Programs;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIResponse.UIResponseObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.USPharmResponse.USPharmPrice;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.ZipCodeLatLongResponse.ZipCodeLatLngResponse;
import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import com.galaxe.drugpriceapi.src.TableModels.Price;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.USPharmResponse.USPharmResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.InsideRxResponse.InsideRxResponse;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.MedimpactResponse.LocatedDrug;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.UIRequest.UIRequestObject;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.SinglecareResponse.PharmacyPricings;
import com.galaxe.drugpriceapi.src.ResponseRequestObjects.WellRxResponse.Drugs;
import org.decimal4j.util.DoubleRounder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.CollectionUtils;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@CrossOrigin
@RestController
public class PriceService {
    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    DrugMasterController drugMasterController;
    @Autowired
    BlinkClient blinkClient;
    @Autowired
    GoodRxService goodRxService;
    @Autowired
    InsideRxService insideRxService;
    @Autowired
    MedImpactService medImpactService;
    @Autowired
    SinglecareService singlecareService;
    @Autowired
    WellRxService wellRxService;
    @Autowired
    USPharmService usPharmService;

    private final String GENERIC = "GENERIC";

    private final String BRAND_WITH_GENERIC = "BRAND_WITH_GENERIC";

    private final String G = "G";

    private final String B = "B";

    private final String NA = "N/A";

    private final String ZERO = "0.0";

    private final String CSRF_TOKEN = "Hi6yGXfg-vppErZsd2KXvKmH9LxjPBNJeK48";

    private final String COOKIE = "_gcl_au=1.1.1639244140.1555443999; _fbp=fb.1.1555443999440.65320427; _ga=GA1.2.711100002.1555444000; _gid=GA1.2.317294123.1555444000; _hjIncludedInSample=1; _csrf=Z3iefjYKIjIUIEXBJgTix0BY; _gat_UA-113293481-1=1; geocoords=40.7473758%2C-74.05057520000003; AWSALB=6NBPPYHYpRwHG5ONO7yvFP6fzmSCfiDRLUr3FCKprscG4ld2CKg2lU+ZRCxhxrTF55clcMF7APSLyeZBhLeH2pv/9pzCIWt8u9lcfJfF8La8Z/eIpABRoF3orpJj";

    private Map<String, Map<String, String>> longLatMap = new HashMap<>();

    private Map<String, List<DrugBrand>> drugBrandInfoMap = new HashMap<>();



    public Price updatePrice(Price price) throws ExecutionException, InterruptedException {

        DrugMaster drugMaster = drugMasterRepository.findById(price.getDrugDetailsId()).get();
        Price newPrice = new Price();
        newPrice.setCreatedat(new Date());
        newPrice.setProgramId(price.getProgramId());
        newPrice.setDrugDetailsId(price.getDrugDetailsId());

        UIRequestObject UIRequestObject = new UIRequestObject();
        UIRequestObject.setQuantity(drugMaster.getQuantity());
        UIRequestObject.setDrugType(drugMaster.getDrugType());
        UIRequestObject.setDosageStrength(drugMaster.getDosageStrength());
        UIRequestObject.setDrugName(drugMaster.getName());
        UIRequestObject.setDrugNDC(drugMaster.getNdc());
        UIRequestObject.setZipcode(drugMaster.getZipCode());

        Map<String, String> longitudeLatitude = constructLongLat(UIRequestObject.getZipcode());
        String brandType = getBrandIndicator(UIRequestObject).intern();
        if(brandType.equals("B")){
            UIRequestObject.setDrugType("BRAND_WITH_GENERIC");
        }else{
            UIRequestObject.setDrugType("GENERIC");
        }

        if (price.getProgramId() == 0) {
            UIRequestObject.setProgram("insideRx");
            CompletableFuture<List<InsideRxResponse>> inside = insideRxService.constructInsideRxWebClient(UIRequestObject, longitudeLatitude);
            CompletableFuture.allOf(inside).join();
            try {
                List<InsideRxResponse> insideRxPrices = inside.get();
                newPrice.setPrice(Double.parseDouble(insideRxPrices.get(0).getPrices().get(0).getPrice()));
                newPrice.setPharmacy(insideRxPrices.get(0).getPrices().get(0).getPharmacy().getName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 1) {
            UIRequestObject.setProgram("usPharmacyCard");
            CompletableFuture<List<USPharmResponse>> usPharmacy = usPharmService.constructUsPharmacy(UIRequestObject);

            CompletableFuture.allOf(usPharmacy).join();
            try {
                List<USPharmResponse> usPharmacyPrices = usPharmacy.get();
                drugMaster = drugMasterRepository.findById(drugMaster.getId()).get();
                drugMaster.setDosageUOM(usPharmacyPrices.get(0).getDosage().getDosageUOM());
                drugMaster = drugMasterRepository.save(drugMaster);
                newPrice.setPrice(Double.parseDouble(usPharmacyPrices.get(0).getPriceList().get(0).getDiscountPrice()));
                newPrice.setPharmacy(usPharmacyPrices.get(0).getPriceList().get(0).getPharmacy().getPharmacyName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 2) {
            CompletableFuture<List<Drugs>> wellRxFuture = wellRxService.getWellRxDrugInfo(UIRequestObject, longitudeLatitude, brandType);
            UIRequestObject.setProgram("wellRx");
            CompletableFuture.allOf(wellRxFuture).join();
            try {
                List<Drugs> wellRx = wellRxFuture.get();
                newPrice.setPrice(Double.parseDouble(wellRx.get(0).getPrice()));
                newPrice.setPharmacy(wellRx.get(0).getPharmacyName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 3) {
            CompletableFuture<LocatedDrug> medImpactFuture = medImpactService.getMedImpact(UIRequestObject, longitudeLatitude, brandType);
            UIRequestObject.setProgram("medImpact");
            CompletableFuture.allOf(medImpactFuture).join();
            try {
                LocatedDrug locatedDrug = medImpactFuture.get();
                newPrice.setPrice(Double.parseDouble(locatedDrug.getPricing().getPrice()));
                newPrice.setPharmacy(locatedDrug.getPharmacy().getName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 4) {
            CompletableFuture<PharmacyPricings> singleCareFuture = singlecareService.getSinglecarePrices(UIRequestObject);
            UIRequestObject.setProgram("medImpact");
            CompletableFuture.allOf(singleCareFuture).join();
            try {
                PharmacyPricings singleCarePrice = singleCareFuture.get();

                newPrice.setPrice(Double.parseDouble(singleCarePrice.getPrices().get(0).getPrice()));
                newPrice.setPharmacy(singleCarePrice.getPharmacy().getName());

            } catch (Exception e) {
                return null;
            }
        } else if (price.getProgramId() == 5) {
            CompletableFuture<BlinkResponse> blinkFuture = null;
            blinkFuture = blinkClient.getBlinkPharmacyPrice(UIRequestObject);

            BlinkResponse blinkResponse = blinkFuture.get();
            try {
                newPrice.setPrice(Double.parseDouble(blinkResponse.getPrice().getLocal().getRaw_value()));
                newPrice.setPharmacy("Blink");
            } catch (Exception e) {
                return null;
            }

        }

        return newPrice;


    }

    public String getPharmacyPrice(String providerName, DrugMaster drugMaster, UIRequestObject UIRequestObject, Map<String, String> longitudeLatitude) {

        try {
            String brandType = drugMaster.getDrugType();
            if (providerName.equals("InsideRxResponse")) {
                UIRequestObject.setProgram("insideRx");
                CompletableFuture<List<InsideRxResponse>> inside = insideRxService.constructInsideRxWebClient(UIRequestObject, longitudeLatitude);
                CompletableFuture.allOf(inside).join();
                List<InsideRxResponse> insideRxPrices = inside.get();

                return insideRxPrices.get(0).getPrices().get(0).getPrice();

            } else if (providerName.equals("U.S Pharmacy Card")) {
                CompletableFuture<List<USPharmResponse>> usPharmacy = usPharmService.constructUsPharmacy(UIRequestObject);
                UIRequestObject.setProgram("usPharmacyCard");
                //      requestObject.setDrugType("BRAND_WITH_GENERIC");
                CompletableFuture.allOf(usPharmacy).join();
                try {
                    List<USPharmResponse> usPharmacyPrices = usPharmacy.get();
                    if (drugMaster.getDosageUOM() == null || drugMaster.getDosageUOM().equals("")) {
                        drugMaster = drugMasterRepository.findById(drugMaster.getId()).get();
                        drugMaster.setDosageUOM(usPharmacyPrices.get(0).getDosage().getDosageUOM());
                        drugMasterRepository.save(drugMaster);
                    }
                    return usPharmacyPrices.get(0).getPriceList().get(0).getDiscountPrice();
                } catch (Exception e) {
                    return "";
                }

            } else if (providerName.equals("WellRx")) {
                if (brandType.equals("BRAND_WITH_GENERIC")) {
                    brandType = "B";
                } else {
                    brandType = "G";
                }
                CompletableFuture<List<Drugs>> wellRxFuture = wellRxService.getWellRxDrugInfo(UIRequestObject, longitudeLatitude, brandType);
                UIRequestObject.setProgram("wellRx");
                CompletableFuture.allOf(wellRxFuture).join();
                List<Drugs> wellRx = wellRxFuture.get();
                return wellRx.get(0).getPrice() + " ";

            } else if (providerName.equals("MedImpact")) {
                CompletableFuture<LocatedDrug> medImpactFuture = medImpactService.getMedImpact(UIRequestObject, longitudeLatitude, brandType);
                UIRequestObject.setProgram("medImpact");

                CompletableFuture.allOf(medImpactFuture).join();
                LocatedDrug locatedDrug = medImpactFuture.get();

                return locatedDrug.getPricing().getPrice();

            } else if (providerName.equals("SingleCare")) {
                CompletableFuture<PharmacyPricings> singleCareFuture = singlecareService.getSinglecarePrices(UIRequestObject);
                UIRequestObject.setProgram("singleCare");
                CompletableFuture.allOf(singleCareFuture).join();
                PharmacyPricings singleCarePrice = singleCareFuture.get();

                return singleCarePrice.getPrices().get(0).getPrice();

            } else if (providerName.equals("InsideRxResponse")) {
                CompletableFuture<BlinkResponse> blinkFuture = null;
                blinkFuture = blinkClient.getBlinkPharmacyPrice(UIRequestObject);
                BlinkResponse blinkResponse = blinkFuture.get();
                try {
                    return blinkResponse.getPrice().getLocal().getRaw_value();
                } catch (Exception e) {
                    return "";
                }

            }
            return " ";
        } catch (Exception e) {
            return " ";
        }
    }

    private Price savePrice(Price p) {
        if (p == null) {
            return null;
        }
        p.setCreatedat(new Date());
        return priceRepository.save(p);
    }
    public List<Price> addPrices(UIRequestObject UIRequestObject, DrugMaster drugMaster) throws Throwable {
        PricesAndMaster details = getDetails(UIRequestObject, drugMaster);
        List<Price> addedPrices = new ArrayList<>();

        for (Price price : details.getPrices()) {
            addedPrices.add(savePrice(price));
        }
        Double lowestPrice = addedPrices.get(0).getPrice();
        Double averagePrice;
        Double sum= 0.0;
        int count = 0;
        for(Price p2 : addedPrices) {
            if(p2 == null ){

            }else{
                count++;
                sum = sum+p2.getPrice();
                if(p2.getPrice() <= lowestPrice){
                    lowestPrice = p2.getPrice();
                }
            }
        }
        averagePrice = sum/ count;

        for(Price p2 : addedPrices){
            if(p2 != null) {
                p2.setRecommendedPrice(lowestPrice);
                p2.setAveragePrice(averagePrice);
                priceRepository.save(p2);
            }
        }

        return addedPrices;
    }
    public UIResponseObject getFinalDrug(UIRequestObject UIRequestObject) throws Throwable {
        Map<String, String> longitudeLatitude = constructLongLat(UIRequestObject.getZipcode());
        String brandType = getBrandIndicator(UIRequestObject).intern();

        if (brandType.isEmpty()) {
            brandType = B;
            UIRequestObject.setDrugType(BRAND_WITH_GENERIC);
        } else {
            UIRequestObject.setDrugType(brandType.equalsIgnoreCase(G) ? GENERIC : BRAND_WITH_GENERIC);
        }


        CompletableFuture<BlinkResponse> blinkFuture = null;

        CompletableFuture<List<InsideRxResponse>> inside = insideRxService.constructInsideRxWebClient(UIRequestObject, longitudeLatitude);
        CompletableFuture<List<USPharmResponse>> usPharmacy = usPharmService.constructUsPharmacy(UIRequestObject);
        CompletableFuture<List<Drugs>> wellRxFuture = wellRxService.getWellRxDrugInfo(UIRequestObject, longitudeLatitude, brandType);
        CompletableFuture<LocatedDrug> medImpactFuture = medImpactService.getMedImpact(UIRequestObject, longitudeLatitude, brandType);
        CompletableFuture<PharmacyPricings> singleCareFuture = singlecareService.getSinglecarePrices(UIRequestObject);
        CompletableFuture<GoodRxResponse> goodRxFuture = goodRxService.getGoodRxPrices(UIRequestObject);

        blinkFuture = blinkClient.getBlinkPharmacyPrice(UIRequestObject);


        //Wait until they are all done
        if (blinkFuture != null)
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture, blinkFuture, goodRxFuture).join();
        else {
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture,goodRxFuture).join();
            //   start = System.currentTimeMillis();
        }


        //List and obj to store future result
        List<InsideRxResponse> insideRxPrices = inside.get();
        List<USPharmResponse> usPharmacyPrices = usPharmacy.get();
        List<Drugs> wellRx = wellRxFuture.get();

        LocatedDrug locatedDrug = medImpactFuture.get();
        PharmacyPricings singleCarePrice = singleCareFuture.get();
        BlinkResponse blinkResponse = null;
        GoodRxResponse goodRxResponse = goodRxFuture.get();
        if (blinkFuture != null)
            blinkResponse = blinkClient.getBlinkPharmacyPrice(UIRequestObject).get();

        UIResponseObject entity = constructEntity(usPharmacyPrices, insideRxPrices, UIRequestObject, wellRx, locatedDrug, singleCarePrice, blinkResponse, goodRxResponse);//BADDDDDD

        return entity;

    }
    public void  createDrugRequests(UIRequestObject UIRequestObject){
        Map<String, String> longitudeLatitude = constructLongLat(UIRequestObject.getZipcode());
        String brandType = getBrandIndicator(UIRequestObject).intern();


        try {
            CompletableFuture<List<InsideRxResponse>> inside = insideRxService.constructInsideRxWebClient(UIRequestObject, longitudeLatitude);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CompletableFuture<List<USPharmResponse>> usPharmacy = usPharmService.constructUsPharmacy(UIRequestObject);
        CompletableFuture<List<Drugs>> wellRxFuture = wellRxService.getWellRxDrugInfo(UIRequestObject, longitudeLatitude, brandType);
        CompletableFuture<LocatedDrug> medImpactFuture = medImpactService.getMedImpact(UIRequestObject, longitudeLatitude, brandType);
        CompletableFuture<PharmacyPricings> singleCareFuture = singlecareService.getSinglecarePrices(UIRequestObject);
        CompletableFuture<GoodRxResponse> goodRxFuture = goodRxService.getGoodRxPrices(UIRequestObject);

    }
    public String getDrugInfoFromInsideRx(WebClient webClient) {
        return webClient
                .get()
                .header("csrf-token", CSRF_TOKEN)
                .header("Cookie", COOKIE)
                .retrieve().bodyToMono(String.class)
                .block();
    }

    public String getBrandIndicator(UIRequestObject UIRequestObject) {
        List<DrugBrand> drugBrands = getDrugInfoFromUs(UIRequestObject.getDrugName().replace("Hydrochloride", "HCL"));
        StringBuilder sb = new StringBuilder();
        drugBrands.forEach(brand -> {

            if (brand.getProductName().equalsIgnoreCase(UIRequestObject.getDrugName())) {
                sb.append(brand.getDrugType().equalsIgnoreCase(BRAND_WITH_GENERIC) ? B : G);
                return;
            }

        });
        if(!sb.toString().equals(B) && !sb.toString().equals(G)){
            return G;
        }
        return sb.toString();
    }

    public Map<String, String> constructLongLat(String zip) {

        if (longLatMap.containsKey(zip)) {
            return longLatMap.get(zip);
        } else {

            WebClient webClient = WebClient.create("https://api.promaptools.com/service/us/zip-lat-lng/get/?zip=" + zip + "&key=17o8dysaCDrgv1c");
            List<ZipCodeLatLngResponse> longLat = webClient.get().exchange().flatMapMany(clientResponse -> clientResponse.bodyToFlux(ZipCodeLatLngResponse.class)).collectList().block();

            Map<String, String> map = new HashMap<>();
            map.put("longitude", longLat.get(0).getOutput().get(0).getLongitude());
            map.put("latitude", longLat.get(0).getOutput().get(0).getLatitude());
            longLatMap.put(zip, map);
            return map;
        }

    }

    private List<DrugBrand> getDrugInfoFromUs(String name) {

        if (drugBrandInfoMap.containsKey(name)) {
            return drugBrandInfoMap.get(name);
        } else {
            name.replace("/", "%2F");
            WebClient webClient = WebClient.create("https://api.uspharmacycard.com/drug/lookup/name/" + name.replace("/", "%2F"));
            List<DrugBrand> brands = webClient
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(DrugBrand.class))
                    .collectList().block();

            drugBrandInfoMap.put(name, brands);
            return brands;
        }
    }


    public UIResponseObject constructEntity
            (List<USPharmResponse> usCardProgramResult, List<InsideRxResponse> insideRxProgramResult,
             UIRequestObject reqObject, List<Drugs> wellRxProgramResult, LocatedDrug medImpactLocatedDrug,
             PharmacyPricings singlecarePrice, BlinkResponse blinkResponse, GoodRxResponse goodRxResponse) {

        UIResponseObject finalDrugObject = new UIResponseObject();
        Double sum = 0.0;
        String recommended;
        SortedSet<Double> recommendedPriceSet = new TreeSet<>();

        List<Programs> programs = new ArrayList<>();
        finalDrugObject.setName(reqObject.getDrugName());

        if (!CollectionUtils.isEmpty(insideRxProgramResult.get(0).getPrices())) {
            List<PriceDetails> programsList = new ArrayList<>();
            programsList.add(new PriceDetails("insideRx", insideRxProgramResult.get(0).getPrices().get(0).getPharmacy().getName().toUpperCase(), insideRxProgramResult.get(0).getPrices().get(0).getPrice(),insideRxProgramResult.get(0).getPrices().get(0).getUncPrice(),false, "0.0", "0.0"));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

            recommendedPriceSet.add(Double.parseDouble(insideRxProgramResult.get(0).getPrices().get(0).getPrice()));
        } else {
            List<PriceDetails> programsList = new ArrayList<>();
            programsList.add(new PriceDetails("insideRx", NA, NA,NA, false, ZERO,  ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }
        try {
            if (!CollectionUtils.isEmpty(usCardProgramResult.get(0).getPriceList())) {
                List<PriceDetails> programsList = new ArrayList<>();
                programsList.add(new PriceDetails("usPharmacyCard", usCardProgramResult.get(0).getPriceList().get(0).getPharmacy().getPharmacyName().toUpperCase(), usCardProgramResult.get(0).getPriceList().get(0).getDiscountPrice(),NA,false, "0.0", "0.0"));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);

                recommendedPriceSet.add(Double.parseDouble(usCardProgramResult.get(0).getPriceList().get(0).getDiscountPrice()));
            } else {
                List<PriceDetails> programsList = new ArrayList<>();
                programsList.add(new PriceDetails("usPharmacyCard", NA, NA,NA, false,ZERO, ZERO));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);

            }
        } catch (Exception ex) {
            List<PriceDetails> programsList = new ArrayList<>();
            programsList.add(new PriceDetails("usPharmacyCard", NA, NA, NA, false, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }
        if (!CollectionUtils.isEmpty(wellRxProgramResult)) {
            List<PriceDetails> programsList = new ArrayList<>();
            programsList.add(new PriceDetails("wellRx", wellRxProgramResult.get(0).getPharmacyName().toUpperCase(), wellRxProgramResult.get(0).getPrice(),NA,false, "0.0", "0.0"));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);
            recommendedPriceSet.add(Double.parseDouble(wellRxProgramResult.get(0).getPrice()));
        } else {
            List<PriceDetails> programsList = new ArrayList<>();
            programsList.add(new PriceDetails("wellRx", NA, NA,NA,false, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }

        if (medImpactLocatedDrug != null) {
            if (medImpactLocatedDrug.getPricing() != null && medImpactLocatedDrug.getPharmacy() != null) {
                recommendedPriceSet.add(Double.parseDouble(medImpactLocatedDrug.getPricing().getPrice()));
                List<PriceDetails> programsList = new ArrayList<>();
                programsList.add(new PriceDetails("medImpact", medImpactLocatedDrug.getPharmacy().getName().toUpperCase(), medImpactLocatedDrug.getPricing().getPrice(), NA,false, "0.0", "0.0"));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);

            } else {
                List<PriceDetails> programsList = new ArrayList<>();
                programsList.add(new PriceDetails("medImpact", NA, NA,NA,false, ZERO, ZERO));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);


            }
        } else {
            List<PriceDetails> programsList = new ArrayList<>();
            programsList.add(new PriceDetails("medImpact", NA, NA,NA, false, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }

        if (singlecarePrice != null) {
            if (!CollectionUtils.isEmpty(singlecarePrice.getPrices()) && singlecarePrice.getPharmacy() != null) {
                recommendedPriceSet.add(Double.parseDouble(singlecarePrice.getPrices().get(0).getPrice()));
                List<PriceDetails> programsList = new ArrayList<>();
                programsList.add(new PriceDetails("singlecare", singlecarePrice.getPharmacy().getName().toUpperCase(), singlecarePrice.getPrices().get(0).getPrice(),NA,false, "0.0", "0.0"));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);
            } else {
                List<PriceDetails> programsList = new ArrayList<>();
                programsList.add(new PriceDetails("singlecare", NA, NA,NA, false, ZERO, ZERO));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);
            }
        } else {
            List<PriceDetails> programsList = new ArrayList<>();
            programsList.add(new PriceDetails("singlecare", NA, NA, NA, false, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }

        if (blinkResponse != null) {
            if (blinkResponse.getPrice() != null && blinkResponse.getPharmacyDetails() != null) {
                try {
                    recommendedPriceSet.add(Double.parseDouble(blinkResponse.getPrice().getLocal().getRaw_value()));
                    List<PriceDetails> programsList = new ArrayList<>();
                    programsList.add(new PriceDetails("blink", blinkResponse.getPharmacyDetails().getName(), blinkResponse.getPrice().getLocal().getRaw_value(),NA,false, ZERO, ZERO));
                    Programs p = new Programs();
                    p.setPrices(programsList);
                    programs.add(p);
                } catch (NullPointerException e) {
                    List<PriceDetails> programsList = new ArrayList<>();
                    programsList.add(new PriceDetails("blink", NA, NA,NA, false, ZERO, ZERO));
                    Programs p = new Programs();
                    p.setPrices(programsList);
                    programs.add(p);


                }
            } else {
                List<PriceDetails> programsList = new ArrayList<>();
                programsList.add(new PriceDetails("blink",NA, NA, NA,false, ZERO, ZERO));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);

            }
        } else {
            List<PriceDetails> programsList = new ArrayList<>();
            programsList.add(new PriceDetails("blink", NA, NA,NA, false, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);
        }

        if (goodRxResponse != null) {
//            WebClient webClient = WebClient.create("https://api.uspharmacycard.com/drug/lookup/name/");
            System.out.println("good");
        }
        List<PriceDetails> programsList = new ArrayList<>();
        programsList.add(new PriceDetails("GoodRx", "N/A", NA,NA, false, ZERO, ZERO));
        Programs p = new Programs();
        p.setPrices(programsList);
        programs.add(p);

        finalDrugObject.setPrograms(programs);

        try {
            recommended = String.valueOf(DoubleRounder.round(recommendedPriceSet.first(), 2));

            for (Double price : recommendedPriceSet) {
                sum += price;
            }

            finalDrugObject.setAverage(String.valueOf(DoubleRounder.round(sum / recommendedPriceSet.size(), 2)));
        } catch (NoSuchElementException e) {
            recommended = "0.00";
        }
        Set<String> pharmacyListAlexa = new HashSet<>();

        if (!CollectionUtils.isEmpty(insideRxProgramResult.get(0).getPrices())) {
            for (InsideRxPrice inside : insideRxProgramResult.get(0).getPrices()) {
                Double d = Double.parseDouble(inside.getPrice());
                if (recommended.equalsIgnoreCase(String.valueOf(DoubleRounder.round(d, 2)))) {
                    String pharmacy = inside.getPharmacy().getName() + " " + inside.getPharmacy().getAddressLine1() + " " + inside.getPharmacy().getCity();
                    pharmacyListAlexa.add(pharmacy);
                } else {
                    break;
                }
            }
        }
        if (usCardProgramResult.size() >= 1) {
            if (!CollectionUtils.isEmpty(usCardProgramResult.get(0).getPriceList())) {

                for (USPharmPrice us : usCardProgramResult.get(0).getPriceList()) {
                    Double d = Double.parseDouble(us.getDiscountPrice());
                    if (recommended.equalsIgnoreCase(String.valueOf(DoubleRounder.round(d, 2)))) {
                        String pharmacy = us.getPharmacy().getPharmacyName() + " " + us.getPharmacy().getAddress() + " " + us.getPharmacy().getCity();
                        pharmacyListAlexa.add(pharmacy);
                    } else {
                        break;
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(wellRxProgramResult)) {

            for (Drugs wellRx : wellRxProgramResult) {
                Double d = Double.parseDouble(wellRx.getPrice());
                if (recommended.equalsIgnoreCase(String.valueOf(DoubleRounder.round(d, 2)))) {
                    String pharmacy = wellRx.getPharmacyName() + " " + wellRx.getAddress() + " " + wellRx.getCity();
                    pharmacyListAlexa.add(pharmacy);
                } else {
                    break;
                }
            }
        }

        finalDrugObject.setPharmacyName(pharmacyListAlexa);
        finalDrugObject.setRecommendedPrice(recommended);
        finalDrugObject.setDrugType(!CollectionUtils.isEmpty(wellRxProgramResult) ? (wellRxProgramResult.get(0).getBrandGeneric().trim().equalsIgnoreCase(G)
                ? GENERIC : BRAND_WITH_GENERIC) : reqObject.getDrugType());
        String dosageStrength = reqObject.getDosageStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%]", "").trim().intern();
        finalDrugObject.setDosageStrength(dosageStrength);
        if (usCardProgramResult.size() >= 1) {
            finalDrugObject.setDosageUOM(CollectionUtils.isEmpty(usCardProgramResult.get(0).getPriceList()) ? "" : usCardProgramResult.get(0).getDosage().getDosageUOM().toUpperCase());
        }
        finalDrugObject.setNdc(reqObject.getDrugNDC());
        finalDrugObject.setQuantity(String.valueOf(reqObject.getQuantity()));
        finalDrugObject.setZipcode(reqObject.getZipcode());
        finalDrugObject.setId(finalDrugObject.getCompositeId(finalDrugObject.getNdc(), finalDrugObject.getDosageStrength(), finalDrugObject.getQuantity(), finalDrugObject.getZipcode()));
        if (!programs.get(0).getPrices().get(0).getPrice().equalsIgnoreCase("N/A")) {
            finalDrugObject.setRecommendedDiff(String.valueOf(Double.parseDouble(recommended) - Double.parseDouble(programs.get(0).getPrices().get(0).getPrice())));
        }
        finalDrugObject.setAverageDiff("0.0");

        return finalDrugObject;

    }

    public PricesAndMaster getDetails(UIRequestObject UIRequestObject, DrugMaster drugMaster) throws Throwable {

        long start = System.currentTimeMillis();
        Map<String, String> longitudeLatitude = new HashMap<>();
        longitudeLatitude.put("longitude", UIRequestObject.getLongitude());
        longitudeLatitude.put("latitude", UIRequestObject.getLatitude());

        start = System.currentTimeMillis();
        String brandType = getBrandIndicator(UIRequestObject).intern();

        start = System.currentTimeMillis();
        if (brandType.isEmpty()) {
            brandType = "B";
            UIRequestObject.setDrugType("BRAND_WITH_GENERIC");
        } else {
            UIRequestObject.setDrugType(brandType.equalsIgnoreCase("G") ? "GENERIC" : "BRAND_WITH_GENERIC");
        }

        start = System.currentTimeMillis();
        CompletableFuture<BlinkResponse> blinkFuture = null;



        CompletableFuture<List<InsideRxResponse>> inside = null;
        try {
            inside = insideRxService.constructInsideRxWebClient(UIRequestObject, longitudeLatitude);
        }catch (Exception ex){
            inside = CompletableFuture.completedFuture(new ArrayList<>());
        }
        CompletableFuture<List<USPharmResponse>> usPharmacy = null;
        try {
            usPharmacy = usPharmService.constructUsPharmacy(UIRequestObject);
        }catch (Exception ex){
            usPharmacy = CompletableFuture.completedFuture(new ArrayList<>());
        }

        CompletableFuture<List<Drugs>> wellRxFuture  = null;
        try {
            wellRxFuture =   wellRxService.getWellRxDrugInfo(UIRequestObject, longitudeLatitude, brandType);
        }catch (Exception ex){
            wellRxFuture = CompletableFuture.completedFuture(new ArrayList<>());
        }
        CompletableFuture<LocatedDrug> medImpactFuture  = null;
        try {
            medImpactFuture = medImpactService.getMedImpact(UIRequestObject, longitudeLatitude, brandType);
        }catch (Exception ex){
            medImpactFuture = CompletableFuture.completedFuture(new LocatedDrug());
        }
        CompletableFuture<PharmacyPricings> singleCareFuture  = null;
        try {
            singleCareFuture =   singlecareService.getSinglecarePrices(UIRequestObject);
        }catch (Exception ex){
            singleCareFuture = CompletableFuture.completedFuture(new PharmacyPricings());
        }

        CompletableFuture<GoodRxResponse> goodRxFuture  = null;
        try {
            goodRxFuture =   goodRxService.getGoodRxPrices(UIRequestObject);
        }catch (Exception ex){
            goodRxFuture = CompletableFuture.completedFuture(new GoodRxResponse());
        }
        try {
            blinkFuture = blinkClient.getBlinkPharmacyPrice(UIRequestObject);
        }catch (Exception ex){
            blinkFuture = CompletableFuture.completedFuture(new BlinkResponse());
        }

        //Wait until they are all done
        if (blinkFuture != null)
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture,goodRxFuture, blinkFuture).join();
        else {
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture,goodRxFuture).join();
            //   start = System.currentTimeMillis();
        }


        //List and obj to store future result
        List<InsideRxResponse> insideRxPrices = inside.get();
        List<USPharmResponse> usPharmacyPrices = usPharmacy.get();
        List<Drugs> wellRx = wellRxFuture.get();
        LocatedDrug locatedDrug = medImpactFuture.get();
        PharmacyPricings singleCarePrice = singleCareFuture.get();

        GoodRxResponse goodRxPrice;
        try {
            if(UIRequestObject.getDrugName().toUpperCase().equals("ATORVASTATIN CALCIUM")) {
                System.out.println("ACYCLOVIR");
            }
            if(UIRequestObject.getDrugName().toUpperCase().equals("CIPROFLOXACIN HCL")) {
                System.out.println("ACYCLOVIR");
            }
            if(UIRequestObject.getDrugName().toUpperCase().equals("CIPROFLOXACIN HYDROCHLORIDE")) {
                System.out.println("ACYCLOVIR");
            }
            if(UIRequestObject.getDrugName().toUpperCase().equals("CITALOPRAM HYDROBROMIDE")) {
                System.out.println("ACYCLOVIR");
            }
            if(UIRequestObject.getDrugName().toUpperCase().equals("CLOMIPHENE CITRATE")) {
                System.out.println("ACYCLOVIR");
            }
            if(UIRequestObject.getDrugName().toUpperCase().equals("ERGOCALCIFEROL")) {
                System.out.println("ACYCLOVIR");
            }
            goodRxPrice  =goodRxFuture.get();

        }catch (Exception ex){
            goodRxPrice = new GoodRxResponse();
        }
        BlinkResponse blinkResponse = null;
        if (blinkFuture != null)
            blinkResponse = blinkClient.getBlinkPharmacyPrice(UIRequestObject).get();

        PricesAndMaster pricesAndMaster = new PricesAndMaster();
        List<Price> prices = new ArrayList<>();


        Price p = new Price();
        try {
            InsideRxResponse insideRxResponse = insideRxPrices.get(0);
            p.setPrice(Double.parseDouble(insideRxResponse.getPrices().get(0).getPrice()));
            p.setPharmacy(insideRxResponse.getPrices().get(0).getPharmacy().getName());
            p.setDrugDetailsId(drugMaster.getId());
            p.setProgramId(0);
            p.setCreatedat(new Date());
        } catch (Exception e) {
            p = null;
        }

        Price p1 = new Price();
        try {
            USPharmResponse usPharm = usPharmacyPrices.get(0);
            p1.setPrice(Double.parseDouble(usPharm.getPriceList().get(0).getDiscountPrice()));
            p1.setPharmacy(usPharm.getPriceList().get(0).getPharmacy().getPharmacyName());
            p1.setProgramId(1);
            p1.setDrugDetailsId(drugMaster.getId());
            p1.setCreatedat(new Date());
        } catch (Exception e) {
            p1 = null;
        }

        Price p2 = new Price();
        try {
            Drugs well = wellRx.get(0);

            if(UIRequestObject.getDrugName().equalsIgnoreCase("Genotropin") && UIRequestObject.getDosageStrength().contains("1.6")){System.out.println("GOT Drug");}
            p2.setPrice(Double.parseDouble(well.getPrice()));
            if(UIRequestObject.getDrugName().equalsIgnoreCase("Genotropin") && UIRequestObject.getDosageStrength().contains("1.6")){System.out.println("GOT price");}
            p2.setPharmacy(well.getPharmacyName());
            if(UIRequestObject.getDrugName().equalsIgnoreCase("Genotropin") && UIRequestObject.getDosageStrength().contains("1.6")){System.out.println("GOT pharmacy");}
            p2.setProgramId(2);
            p2.setCreatedat(new Date());
            if(UIRequestObject.getDrugName().equalsIgnoreCase("Genotropin") && UIRequestObject.getDosageStrength().contains("1.6")){System.out.println("GOT program");}
            p2.setDrugDetailsId(drugMaster.getId());
            if(UIRequestObject.getDrugName().equalsIgnoreCase("Genotropin") && UIRequestObject.getDosageStrength().contains("1.6")){System.out.println("GOT id");}
        } catch (Exception e) {
            if(UIRequestObject.getDrugName().equalsIgnoreCase("Genotropin") && UIRequestObject.getDosageStrength().contains("1.6")){System.out.println("Return null"+ prices.size());}

            p2 = null;
        }
        //MediIMpact
        Price p3 = new Price();
        try {
            p3.setPrice(Double.parseDouble(locatedDrug.getPricing().getPrice()));
            p3.setPharmacy(locatedDrug.getPharmacy().getName());
            p3.setProgramId(3);
            p3.setDrugDetailsId(drugMaster.getId());
            p3.setCreatedat(new Date());
        } catch (Exception e) {
            p3 = null;
        }
        Price p4 = new Price();
        try {
            p4.setPrice(Double.parseDouble(singleCarePrice.getPrices().get(0).getPrice()));
            p4.setPharmacy(singleCarePrice.getPharmacy().getName());
            p4.setProgramId(4);
            p4.setDrugDetailsId(drugMaster.getId());
            p4.setCreatedat(new Date());
        } catch (Exception e) {
            p4 = null;
        }
        Price p5 = new Price();
        try {
            p5.setPrice(Double.parseDouble(blinkResponse.getPrice().getLocal().getRaw_value()));

            p5.setPharmacy(blinkResponse.getPharmacyDetails().getName());
            p5.setProgramId(5);
            p5.setDrugDetailsId(drugMaster.getId());
            p5.setCreatedat(new Date());
        } catch (Exception e) {
            p5 = null;
        }
        Price p6 = new Price();
        try {

            try {
                p6.setPrice(goodRxPrice.getResults().get(0).getPrices().get(0).getPrice());
            }catch (Exception ex){

            }
            p6.setPharmacy(goodRxPrice.getResults().get(0).getPharmacy().getName());
            p6.setProgramId(6);
            p6.setDrugDetailsId(drugMaster.getId());
            p6.setCreatedat(new Date());
        } catch (Exception e) {
            p6 = null;
        }
        prices.add(p);
        prices.add(p1);
        prices.add(p2);
        prices.add(p3);
        prices.add(p4);
        prices.add(p5);
        prices.add(p6);
        if(UIRequestObject.getDrugName().equalsIgnoreCase("Genotropin") && UIRequestObject.getDosageStrength().contains("1.6")){System.out.println("B4 Prices size"+ prices.size());}

        pricesAndMaster.setDrugMaster(drugMaster);

        pricesAndMaster.setPrices(prices);

        return pricesAndMaster;
//        start = System.currentTimeMillis();
//        MongoEntity entity =  priceController.constructEntity(usPharmacyPrices, insideRxPrices, requestObject, wellRx, locatedDrug, singleCarePrice, blink);
//        MongoEntity newEntity = new MongoEntity();
//
//        start = System.currentTimeMillis();
//        MongoEntity m  =  priceController.updateDiff(entity,requestObject);


    }

}
