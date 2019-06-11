package com.galaxe.drugpriceapi.web.nap.controller;

import com.galaxe.drugpriceapi.model.*;
import com.galaxe.drugpriceapi.repositories.DrugNAPRepository;
import com.galaxe.drugpriceapi.repositories.MongoEntityRepository;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.DrugBrandInfo;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.galaxe.drugpriceapi.web.nap.ui.ZipcodeConverter;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import org.decimal4j.util.DoubleRounder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;


@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PriceController {

    private final String CSRF_TOKEN = "Hi6yGXfg-vppErZsd2KXvKmH9LxjPBNJeK48";

    private final String COOKIE = "_gcl_au=1.1.1639244140.1555443999; _fbp=fb.1.1555443999440.65320427; _ga=GA1.2.711100002.1555444000; _gid=GA1.2.317294123.1555444000; _hjIncludedInSample=1; _csrf=Z3iefjYKIjIUIEXBJgTix0BY; _gat_UA-113293481-1=1; geocoords=40.7473758%2C-74.05057520000003; AWSALB=6NBPPYHYpRwHG5ONO7yvFP6fzmSCfiDRLUr3FCKprscG4ld2CKg2lU+ZRCxhxrTF55clcMF7APSLyeZBhLeH2pv/9pzCIWt8u9lcfJfF8La8Z/eIpABRoF3orpJj";

    private Map<String, Map<String, String>> longLatMap = new HashMap<>();

    private Map<String, List<DrugBrandInfo>> drugBrandInfoMap = new HashMap<>();

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);

    private Boolean flag = true;

    @Autowired
    private MongoEntityRepository mongoEntityRepo;

    @Autowired
    private DrugNAPRepository drugNAPRepository;

    @Autowired
    private APIClient apiService;

    @Autowired
    private APIClient2 apiService2;

    @Autowired
    private APIClient3 apiService3;

    private int count = 0;

    private final String GENERIC = "GENERIC";

    private final String BRAND_WITH_GENERIC = "BRAND_WITH_GENERIC";

    private final String G = "G";

    private final String B = "B";

    private final String NA = "N/A";

    private final String ZERO = "0.0";

    private final SortedSet<Double> recommendedPriceSet = new TreeSet<>();

    private final List<Program> programs = new ArrayList<>();

    private Runnable startBatchJob() {
        Runnable task = () -> {
            count++;
            System.out.println("Task Run count :: " + count);
            List<MongoEntity> entities = mongoEntityRepo.findAll();
            if (!CollectionUtils.isEmpty(entities)) {
                entities.forEach(entity -> {
                    try {
                        addDrugToDashBoard(constructRequestObjectFromMongo(entity));
                    } catch (Throwable t) {
                        System.out.println(t.getCause());
                    }
                });
            }
            System.out.println("Tasked ended count :: " + count);
        };
        return task;
    }


    private RequestObject constructRequestObjectFromMongo(MongoEntity entity) {

        RequestObject obj = new RequestObject();
        obj.setDrugName(entity.getName());
        obj.setDrugNDC(entity.getNdc());
        obj.setDosageStrength(entity.getDosageStrength());
        obj.setQuantity(Integer.parseInt(entity.getQuantity()));
        obj.setZipcode(entity.getZipcode());
        return obj;
    }

    private void setScheduledFutureJob() {
        ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(startBatchJob(), 1, 1, TimeUnit.HOURS);
    }

    //Returns Dashboard Drugs
    @GetMapping("/getAllPharmacy")
    public List<MongoEntity> getAllPharmacyForDashboard() {

        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }
        return mongoEntityRepo.findAll();
    }

    //Getting the drug prices for a particular drug
    @PostMapping("/getPharmacyPrice")
    public MongoEntity getPharmacyList(@RequestBody RequestObject requestObject) throws Throwable {

        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }

        return getFinalDrug(requestObject);
    }

    //When Track-Listing is clicked
    @PostMapping("/addDrugToDashBoard")
    public void addDrugToDashBoard(@RequestBody RequestObject requestObject) throws Throwable {

        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }

        MongoEntity latestAPIResponse = getFinalDrug(requestObject);
        mongoEntityRepo.save(latestAPIResponse);
    }

    //Called when typing in drug name to get suggested Drugs
    @GetMapping("/getDrugInfo/{name}")
    public String getDrugInfo(@PathVariable("name") String name) {

        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }

        WebClient webClient = WebClient.create("https://insiderx.com/request/medications/search?query=" + name + "&limit=8&locale=en-US");
        return getDrugInfoFromInsideRx(webClient);
    }

    //Un-used
    @GetMapping("/getSpecificDrugInfo/{name}")
    public String getSpecificDrugInfo(@PathVariable("name") String name) {

        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }

        WebClient webClient = WebClient.create("https://insiderx.com/request/medications/search?query=" + name + "&limit=1&locale=en-US");
        return getDrugInfoFromInsideRx(webClient);
    }

    //Removes drug from dashboard
    @DeleteMapping("/removeDrug/{id}")
    public void removeDrugFromDashboard(@PathVariable("id") String id) {


        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }

        mongoEntityRepo.deleteById(id);

    }

    //Un-used
    @GetMapping("/getDrugName/{ndc}")
    public String getDrugNameByNdc(@PathVariable("ndc") String ndc) {

        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }

        DrugNAP drugNAP = drugNAPRepository.findByndc(ndc).get();
        if (drugNAP != null)
            return getSpecificDrugInfo(drugNAP.getName());

        return "Did not find any drug for specified NDC";

    }

    //Un-used
    @GetMapping("/getAllNdcNumbers")
    public List<DrugNAP> getAllNdc() {

        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }

        return drugNAPRepository.findAll();
    }

    private MongoEntity getFinalDrug(RequestObject requestObject) throws Throwable {

        long start = System.currentTimeMillis();
        System.out.println("Start time : " + start);
        Map<String, String> longitudeLatitude = constructLongLat(requestObject.getZipcode());
        String brandType = getBrandIndicator(requestObject).intern();

        if (brandType.isEmpty()) {
            brandType = B;
            requestObject.setDrugType(BRAND_WITH_GENERIC);
        } else {
            requestObject.setDrugType(brandType.equalsIgnoreCase(G) ? GENERIC : BRAND_WITH_GENERIC);
        }

        CompletableFuture<Blink> blinkFuture = null;
        //Future result
        CompletableFuture<List<InsideRx>> inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
        CompletableFuture<List<DrugNAP2>> usPharmacy = apiService2.constructUsPharmacy(requestObject);
        CompletableFuture<List<Drugs>> wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
        CompletableFuture<LocatedDrug> medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
        CompletableFuture<PharmacyPricings> singleCareFuture = apiService.getSinglecarePrices(requestObject);
        if (brandType.equalsIgnoreCase("G") || requestObject.getDrugType().equalsIgnoreCase(GENERIC)) {
            blinkFuture = apiService3.getBlinkPharmacyPrice(requestObject);
        }


        //Wait until they are all done
        if (blinkFuture != null)
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture, blinkFuture).join();
        else
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture).join();


        System.out.println("After all API call done : " + (System.currentTimeMillis() - start));
        //List and obj to store future result
        List<InsideRx> insideRxPrices = inside.get();
        List<DrugNAP2> usPharmacyPrices = usPharmacy.get();
        List<Drugs> wellRx = wellRxFuture.get();
        LocatedDrug locatedDrug = medImpactFuture.get();
        PharmacyPricings singleCarePrice = singleCareFuture.get();
        Blink blink = null;
        /*if (blinkFuture != null)
            blink = apiService3.getBlinkPharmacyPrice(requestObject).get();
*/
        MongoEntity entity = constructEntity(usPharmacyPrices, insideRxPrices, requestObject, wellRx, locatedDrug, singleCarePrice, blink);
        return updateDiff(entity, requestObject);

    }

    private String getDrugInfoFromInsideRx(WebClient webClient) {
        return webClient
                .get()
                .header("csrf-token", CSRF_TOKEN)
                .header("Cookie", COOKIE)
                .retrieve().bodyToMono(String.class)
                .block();
    }


    private String getBrandIndicator(RequestObject requestObject) {
        List<DrugBrandInfo> drugBrandInfos = getDrugInfoFromUs(requestObject.getDrugName());
        StringBuilder sb = new StringBuilder();
        drugBrandInfos.forEach(brand -> {

            if (brand.getProductName().equalsIgnoreCase(requestObject.getDrugName())) {
                sb.append(brand.getDrugType().equalsIgnoreCase(BRAND_WITH_GENERIC) ? B : G);
                return;
            }

        });
        return sb.toString();
    }

    private Map<String, String> constructLongLat(String zip) {

        if (longLatMap.containsKey(zip)) {
            return longLatMap.get(zip);
        } else {
            WebClient webClient = WebClient.create("https://api.promaptools.com/service/us/zip-lat-lng/get/?zip=" + zip + "&key=17o8dysaCDrgv1c");
            List<ZipcodeConverter> longLat = webClient.get().exchange().flatMapMany(clientResponse -> clientResponse.bodyToFlux(ZipcodeConverter.class)).collectList().block();
            Map<String, String> map = new HashMap<>();
            map.put("longitude", longLat.get(0).getOutput().get(0).getLongitude());
            map.put("latitude", longLat.get(0).getOutput().get(0).getLatitude());
            longLatMap.put(zip, map);
            return map;
        }

    }

    private List<DrugBrandInfo> getDrugInfoFromUs(String name) {

        if (drugBrandInfoMap.containsKey(name)) {
            return drugBrandInfoMap.get(name);
        } else {

            WebClient webClient = WebClient.create("https://api.uspharmacycard.com/drug/lookup/name/" + name);
            List<DrugBrandInfo> brands = webClient
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(DrugBrandInfo.class))
                    .collectList().block();

            drugBrandInfoMap.put(name, brands);
            return brands;
        }
    }

    private MongoEntity constructEntity
            (List<DrugNAP2> usCardProgramResult, List<InsideRx> insideRxProgramResult,
             RequestObject reqObject, List<Drugs> wellRxProgramResult, LocatedDrug medImpactLocatedDrug,
             PharmacyPricings singlecarePrice, Blink blink) {

        MongoEntity finalDrugObject = new MongoEntity();
        Double sum = 0.0;
        String recommended;
        recommendedPriceSet.clear();
        programs.clear();
        finalDrugObject.setName(reqObject.getDrugName());

        if (!CollectionUtils.isEmpty(insideRxProgramResult.get(0).getPrices())) {
            programs.add(new Program("insideRx", insideRxProgramResult.get(0).getPrices().get(0).getPharmacy().getName().toUpperCase(), insideRxProgramResult.get(0).getPrices().get(0).getPrice(), "0.0", "0.0"));
            recommendedPriceSet.add(Double.parseDouble(insideRxProgramResult.get(0).getPrices().get(0).getPrice()));
        } else {
            programs.add(new Program("insideRx", NA, NA, ZERO, ZERO));
        }

        if (!CollectionUtils.isEmpty(usCardProgramResult.get(0).getPriceList())) {
            programs.add(new Program("usPharmacyCard", usCardProgramResult.get(0).getPriceList().get(0).getPharmacy().getPharmacyName().toUpperCase(), usCardProgramResult.get(0).getPriceList().get(0).getDiscountPrice(), "0.0", "0.0"));
            recommendedPriceSet.add(Double.parseDouble(usCardProgramResult.get(0).getPriceList().get(0).getDiscountPrice()));
        } else {
            programs.add(new Program("usPharmacyCard", NA, NA, ZERO, ZERO));
        }

        if (!CollectionUtils.isEmpty(wellRxProgramResult)) {
            programs.add(new Program("wellRx", wellRxProgramResult.get(0).getPharmacyName().toUpperCase(), wellRxProgramResult.get(0).getPrice(), "0.0", "0.0"));
            recommendedPriceSet.add(Double.parseDouble(wellRxProgramResult.get(0).getPrice()));
        } else {
            programs.add(new Program("wellRx", NA, NA, ZERO, ZERO));
        }

        if (medImpactLocatedDrug != null) {
            if (medImpactLocatedDrug.getPricing() != null && medImpactLocatedDrug.getPharmacy() != null) {
                recommendedPriceSet.add(Double.parseDouble(medImpactLocatedDrug.getPricing().getPrice()));
                programs.add(new Program("medImpact", medImpactLocatedDrug.getPharmacy().getName().toUpperCase(), medImpactLocatedDrug.getPricing().getPrice(), "0.0", "0.0"));
            } else {
                programs.add(new Program("medImpact", NA, NA, ZERO, ZERO));

            }
        } else {
            programs.add(new Program("medImpact", NA, NA, ZERO, ZERO));
        }

        if (singlecarePrice != null) {
            if (!CollectionUtils.isEmpty(singlecarePrice.getPrices()) && singlecarePrice.getPharmacy() != null) {
                recommendedPriceSet.add(Double.parseDouble(singlecarePrice.getPrices().get(0).getPrice()));
                programs.add(new Program("singlecare", singlecarePrice.getPharmacy().getName().toUpperCase(), singlecarePrice.getPrices().get(0).getPrice(), "0.0", "0.0"));
            } else {
                programs.add(new Program("singlecare", NA, NA, ZERO, ZERO));
            }
        } else {
            programs.add(new Program("singlecare", NA, NA, ZERO, ZERO));
        }

        if (blink != null) {
            if (blink.getPrice() != null && blink.getResults() != null) {
                recommendedPriceSet.add(Double.parseDouble(blink.getPrice().getLocal().getRaw_value()));
                programs.add(new Program("blink", blink.getResults().getName(), blink.getPrice().getLocal().getRaw_value(), ZERO, ZERO));
            } else {
                programs.add(new Program("blink", NA, NA, ZERO, ZERO));
            }
        } else {
            programs.add(new Program("blink", NA, NA, ZERO, ZERO));
        }

        programs.add(new Program("GoodRx", "Coming soon", NA, ZERO, ZERO));

        finalDrugObject.setPrograms(programs);

        recommended = String.valueOf(DoubleRounder.round(recommendedPriceSet.first(), 2));

        for (Double p : recommendedPriceSet) {
            sum += p;
        }

        finalDrugObject.setAverage(String.valueOf(DoubleRounder.round(sum / recommendedPriceSet.size(), 2)));

        Set<String> pharmacyListAlexa = new HashSet<>();

        if (!CollectionUtils.isEmpty(insideRxProgramResult.get(0).getPrices())) {
            for (Prices inside : insideRxProgramResult.get(0).getPrices()) {
                Double d = Double.parseDouble(inside.getPrice());
                if (recommended.equalsIgnoreCase(String.valueOf(DoubleRounder.round(d, 2)))) {
                    String pharmacy = inside.getPharmacy().getName() + " " + inside.getPharmacy().getAddressLine1() + " " + inside.getPharmacy().getCity();
                    pharmacyListAlexa.add(pharmacy);
                } else {
                    break;
                }
            }
        }

        if (!CollectionUtils.isEmpty(usCardProgramResult.get(0).getPriceList())) {

            for (PriceList us : usCardProgramResult.get(0).getPriceList()) {
                Double d = Double.parseDouble(us.getDiscountPrice());
                if (recommended.equalsIgnoreCase(String.valueOf(DoubleRounder.round(d, 2)))) {
                    String pharmacy = us.getPharmacy().getPharmacyName() + " " + us.getPharmacy().getAddress() + " " + us.getPharmacy().getCity();
                    pharmacyListAlexa.add(pharmacy);
                } else {
                    break;
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
        finalDrugObject.setDosageUOM(CollectionUtils.isEmpty(usCardProgramResult.get(0).getPriceList()) ? "" : usCardProgramResult.get(0).getDosage().getDosageUOM().toUpperCase());
        finalDrugObject.setNdc(reqObject.getDrugNDC());
        finalDrugObject.setQuantity(String.valueOf(reqObject.getQuantity()));
        finalDrugObject.setZipcode(reqObject.getZipcode());
        finalDrugObject.setId(finalDrugObject.getCompositeId(finalDrugObject.getNdc(), finalDrugObject.getDosageStrength(), finalDrugObject.getQuantity(), finalDrugObject.getZipcode()));
        if (!programs.get(0).getPrice().equalsIgnoreCase("N/A")) {
            finalDrugObject.setRecommendedDiff(String.valueOf(Double.parseDouble(recommended) - Double.parseDouble(programs.get(0).getPrice())));
        }
        finalDrugObject.setAverageDiff("0.0");

        return finalDrugObject;

    }


    private MongoEntity updateDiff(MongoEntity currentObject, RequestObject requestObject) {

        Optional<MongoEntity> mongoObjects = mongoEntityRepo.findByNdcAndDosageStrengthAndQuantityAndZipcode(requestObject.getDrugNDC(),
                requestObject.getDosageStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%]", "").trim(), String.valueOf(requestObject.getQuantity()), requestObject.getZipcode());

        if (mongoObjects.isPresent()) {

            Double aveDiff = 0.0, recommendedDiff = 0.0;

            if (!currentObject.getAverage().equalsIgnoreCase(NA))
                aveDiff = (Double.parseDouble(mongoObjects.get().getAverage()) - Double.parseDouble(currentObject.getAverage()));

            if (!currentObject.getRecommendedPrice().equalsIgnoreCase(NA))
                recommendedDiff = Double.parseDouble(currentObject.getRecommendedPrice()) - (Double.parseDouble(currentObject.getPrograms().get(0).getPrice()));

            currentObject.setAverageDiff(String.valueOf(aveDiff));

            currentObject.setRecommendedDiff(String.valueOf(recommendedDiff));

            if (!CollectionUtils.isEmpty(mongoObjects.get().getPrograms()) && !CollectionUtils.isEmpty(currentObject.getPrograms())) {
                List<Program> mongoPrograms = mongoObjects.get().getPrograms();
                List<Program> currPrograms = currentObject.getPrograms();

                mongoPrograms.forEach(mongo -> {

                    for (Program curr : currPrograms) {

                        if (curr.getProgram().equalsIgnoreCase(mongo.getProgram())) {

                            if (!curr.getPrice().equalsIgnoreCase(NA) && !curr.getPrice().isEmpty()) {

                                Double diff, sum;
                                if (!mongo.getPrice().contains(NA)) {

                                    diff = ((Double.parseDouble(mongo.getPrice())) - Double.parseDouble(curr.getPrice()));
                                    sum = Double.parseDouble(mongo.getPrice()) + Double.parseDouble(curr.getPrice());
                                    double p = (diff / (sum / 2)) * 100d;
                                    curr.setDiff(String.valueOf(diff));
                                    curr.setDiffPerc(String.valueOf(p));

                                }

                            }

                        }
                    }
                });
                currentObject.setPrograms(currPrograms);
            }
        }
        return currentObject;

    }

}
