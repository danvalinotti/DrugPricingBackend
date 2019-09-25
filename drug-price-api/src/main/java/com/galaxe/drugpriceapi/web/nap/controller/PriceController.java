package com.galaxe.drugpriceapi.web.nap.controller;

import com.galaxe.drugpriceapi.model.*;
import com.galaxe.drugpriceapi.repositories.DrugNAPRepository;
import com.galaxe.drugpriceapi.repositories.MongoEntityRepository;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.masterList.BatchDetails;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterList;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterListService;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterListTestController;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.*;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.goodRx.GoodRxResponse;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Price;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.RandomPassword;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.*;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import com.mongodb.Mongo;
import org.decimal4j.util.DoubleRounder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ProfileController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.CollectionUtils;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;

//https://drug-pricing-backend.cfapps.io/
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PriceController {
    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    DrugProfileController drugProfileController;
    @Autowired
    MasterListService masterListService;

    private final String CSRF_TOKEN = "Hi6yGXfg-vppErZsd2KXvKmH9LxjPBNJeK48";

    private final String COOKIE = "_gcl_au=1.1.1639244140.1555443999; _fbp=fb.1.1555443999440.65320427; _ga=GA1.2.711100002.1555444000; _gid=GA1.2.317294123.1555444000; _hjIncludedInSample=1; _csrf=Z3iefjYKIjIUIEXBJgTix0BY; _gat_UA-113293481-1=1; geocoords=40.7473758%2C-74.05057520000003; AWSALB=6NBPPYHYpRwHG5ONO7yvFP6fzmSCfiDRLUr3FCKprscG4ld2CKg2lU+ZRCxhxrTF55clcMF7APSLyeZBhLeH2pv/9pzCIWt8u9lcfJfF8La8Z/eIpABRoF3orpJj";

    private Map<String, Map<String, String>> longLatMap = new HashMap<>();

    private Map<String, List<DrugBrandInfo>> drugBrandInfoMap = new HashMap<>();

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);


    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
    ArrayList<ZonedDateTime> times = new ArrayList<>();
    int nexttimeIndex;


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

    @Autowired
    MasterListTestController masterListTestController;

    @Autowired
    DrugReportController drugReportController;
    private int count = 0;

    private final String GENERIC = "GENERIC";

    private final String BRAND_WITH_GENERIC = "BRAND_WITH_GENERIC";

    private final String G = "G";

    private final String B = "B";

    private final String NA = "N/A";

    private final String ZERO = "0.0";


    private Boolean flag = setScheduledFutureJob();
    private Boolean tokenFlag = setScheduledTokenJob();
    private Runnable startBatchJob() {
        Runnable task = () -> {
            count++;
//            List<MongoEntity> entities = mongoEntityRepo.findAll();
//            if (!CollectionUtils.isEmpty(entities)) {
//                entities.forEach(entity -> {   //For each drug in dashboard
//                    try {//Saves updated drug to dashboard
//                     //   addDrugToDashBoard(constructRequestObjectFromMongo(entity));
//
//                    } catch (Throwable t) {
//
//                    }
//                });
//            }

            try {
                drugReportController.generateReport();
//                masterListService.add();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        return task;
    }
    private Runnable startTokenJob() {
        Runnable task = () -> {

            try {
                System.out.println("TOKEN JOB STARTED");
                drugProfileController.resetTokens();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        return task;
    }


    public RequestObject constructRequestObjectFromMongo(MongoEntity entity) {

        RequestObject obj = new RequestObject();
        obj.setDrugName(entity.getName());
        obj.setDrugNDC(entity.getNdc());
        obj.setDosageStrength(entity.getDosageStrength());
        obj.setQuantity(Double.parseDouble(entity.getQuantity()));//WAS PARSE INT
        obj.setZipcode(entity.getZipcode());
        return obj;
    }

    private void test() {
        ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(startBatchJob(), 1, 1, TimeUnit.HOURS);
    }

    private void test2() {

        ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(startBatchJob(), 1, 1, TimeUnit.SECONDS);
    }

    private boolean setScheduledFutureJob() {
        if (now.compareTo(now.withHour(9).withMinute(0).withSecond(0)) > 0) {
            this.times.add(now.withHour(9).withMinute(0).withSecond(0).plusDays(1));
        } else {
            this.times.add(now.withHour(9).withMinute(0).withSecond(0));
        }
//        if(now.compareTo(now.withHour(20).withMinute(0).withSecond(0)) > 0){
//            this.times.add(now.withHour(5).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(9).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(12).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(16).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(20).withMinute(0).withSecond(0).plusDays(1));
//        }else if(now.compareTo(now.withHour(16).withMinute(0).withSecond(0)) > 0){
//            this.times.add(now.withHour(5).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(9).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(12).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(16).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(20).withMinute(0).withSecond(0));
//        }else if(now.compareTo(now.withHour(12).withMinute(0).withSecond(0)) > 0){
//            this.times.add(now.withHour(5).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(9).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(12).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(16).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(20).withMinute(0).withSecond(0));
//        }else if(now.compareTo(now.withHour(9).withMinute(0).withSecond(0)) > 0){
//            this.times.add(now.withHour(5).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(9).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(12).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(16).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(20).withMinute(0).withSecond(0));
//        }else if(now.compareTo(now.withHour(5).withMinute(0).withSecond(0)) > 0){
//            this.times.add(now.withHour(5).withMinute(0).withSecond(0).plusDays(1));
//            this.times.add(now.withHour(9).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(12).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(16).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(20).withMinute(0).withSecond(0));
//        }else{
//            this.times.add(now.withHour(5).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(9).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(12).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(16).withMinute(0).withSecond(0));
//            this.times.add(now.withHour(20).withMinute(0).withSecond(0));
//        }
        for (int i = 0; i < this.times.size(); i++) {
            ZonedDateTime nextRun = this.times.get(i);
            Duration duration = Duration.between(now, nextRun);
            long initalDelay = duration.getSeconds();

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(startBatchJob(),
                    initalDelay,
//                    TimeUnit.DAYS.toSeconds(2),
                  TimeUnit.DAYS.toSeconds(1),
                    TimeUnit.SECONDS);
        }

        return false;
    }
    private boolean setScheduledTokenJob() {
            long initalDelay = TimeUnit.MINUTES.toSeconds(2);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(startTokenJob(),
                    initalDelay,
                    TimeUnit.HOURS.toSeconds(1),
                    TimeUnit.SECONDS);

        return false;
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
        String drugName = requestObject.getDrugName();
        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }
        DrugMaster m = new DrugMaster();

        try {
            m = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity(),requestObject.getZipcode()).get(0);
        } catch (Exception e) {

        }
        try {
            requestObject.setGSN(m.getGsn());
        } catch (Exception ex) {
        }
        DrugMaster d = new DrugMaster();
        MongoEntity mongoEntity = new MongoEntity();
        List<Programs> programs = new ArrayList<>();
        List<Price> prices = new ArrayList<>();
        try {


            d = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity(),requestObject.getZipcode()).get(0);
            System.out.println("NEWEST REPORT ID "+reportRepository.findFirstByOrderByTimestampDesc().getId());
            prices = priceRepository.findRecentPricesByDrugId(d.getId(), reportRepository.findFirstByOrderByTimestampDesc().getId());
            mongoEntity.setRecommendedDiff("0.00");
            List<Integer> programIds = new ArrayList<>();
            List<Price> duplicatePrices = new ArrayList<>();
            int count = 0;

            Map<Integer, List<Price>> programPrices = new HashMap<>();

            programPrices.put(0,new ArrayList<>());
            programPrices.put(1,new ArrayList<>());
            programPrices.put(2,new ArrayList<>());
            programPrices.put(3,new ArrayList<>());
            programPrices.put(4,new ArrayList<>());
            programPrices.put(5,new ArrayList<>());
            programPrices.put(6,new ArrayList<>());

            for (Price p : prices) {
                programPrices.get(p.getProgramId()).add(p);
            }
            List<Programs> programs1 = new ArrayList<>();
            for (Map.Entry<Integer, List<Price>> programPrice : programPrices.entrySet()) {
                Programs p = new Programs();
                List<Program> progs = new ArrayList<>();
                for (Price price: programPrice.getValue()) {
                    Program prog = new Program();
                    prog.setPharmacy(price.getPharmacy());
                    prog.setProgram(programIdToString(price.getProgramId()));
                    prog.setDiff(price.getDifference()+"");
                    prog.setPrice(price.getPrice()+"");
                    progs.add(prog);

                }
                p.setPrices(progs);
                programs1.add(p);
            }
            mongoEntity.setPrograms(programs1);
//            for(int i = 0 ; i<7; i++) {
//
//
//                programPrices.setPrices(programsList);
//                programs.add(programPrices);
//                String average = getAverage(prices);
//                mongoEntity.setAverage(average);
//
//            }
            int priceIndex = 0;
//            for (int i = 0; i <= 6; i++) {
//                String program = "";
//
//                if (i == 0) {
//                    program = "InsideRx";
//                } else if (i == 1) {
//                    program = "US Pharmacy Card";
//                } else if (i == 2) {
//                    program = "WellRx";
//                } else if (i == 3) {
//                    program = "MedImpact";
//                } else if (i == 4) {
//                    program = "SingleCare";
//                } else if (i == 5) {
//                    program = "Blink";
//                }
//                else if (i == 6) {
//                    program = "GoodRx";
//                }
//                Programs programPrices = new Programs();
//                if (priceIndex< prices.size() &&i == prices.get(priceIndex).getProgramId()) {
//                    try {
//                        Price p = prices.get(priceIndex);
//                        String diffPerc = ((p.getDifference() / (p.getPrice() + p.getDifference())) * 100) + "";
//
//                        List<Program> programsList = new ArrayList<>();
//                        programsList.add(new Program(program, p.getPharmacy(), p.getPrice() + "", p.getDifference() + "", diffPerc));
//
//                        programPrices.setPrices(programsList);
//                        programs.add(programPrices);
//
//                        priceIndex++;
//                    }catch(Exception e){
//                        List<Program> programsList = new ArrayList<>();
//                        programsList.add(new Program(program, "N/A", "N/A", "0.0", "0.0"));
//                        Programs programs1 = new Programs();
//                        programs1.setPrices(programsList);
//                        programs.add(programs1);
//                        priceIndex++;
//                    }
//                } else {
//                    List<Program> programsList = new ArrayList<>();
//                    programsList.add(new Program(program, "N/A", "N/A", "0.0", "0.0"));
//                    Programs programs1 = new Programs();
//                    programs1.setPrices(programsList);
//                    programs.add(programs1);
//                    priceIndex++;
//                }
//            }


//            mongoEntity.setPrograms(programs);
            mongoEntity.setQuantity(requestObject.getQuantity() + "");
            mongoEntity.setNdc(requestObject.getDrugNDC());
            mongoEntity.setDrugType(requestObject.getDrugType());
            mongoEntity.setDosageStrength(requestObject.getDosageStrength());
            mongoEntity.setName(requestObject.getDrugName());
            mongoEntity.setZipcode(requestObject.getZipcode());
//            mongoEntity.setDosageUOM(requestObject.get);
            mongoEntity.setRecommendedPrice(prices.get(0).getRecommendedPrice() + "");
            mongoEntity.setAverage(prices.get(0).getAveragePrice()+"");
            WebClient webClient = WebClient.create("https://insiderx.com/request/medication/"+requestObject.getDrugName().toLowerCase().replace(" ", "-")+"/details?locale=en-US");
            DrugDescription description = webClient.get().exchange().flatMap(clientResponse -> clientResponse.bodyToMono(DrugDescription.class)).block();

            mongoEntity.setDescription(description.getDescription());
            System.out.println("FOUND PRICE FROM DATABASE");
            return mongoEntity;
        } catch (Exception ex) {

            ex.printStackTrace();
        }


        MongoEntity finalDrug = getFinalDrug(requestObject);
        WebClient webClient = WebClient.create("https://insiderx.com/request/medication/"+drugName.toLowerCase().replace(" ", "-").replace("/", "-")+"/details?locale=en-US");
        try {
            DrugDescription description = webClient.get().exchange().flatMap(clientResponse -> clientResponse.bodyToMono(DrugDescription.class)).block();

            finalDrug.setDescription(description.getDescription());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        System.out.println("FOUND PRICE FROM API");
        return finalDrug;
    }

    private String programIdToString(int programId) {
        switch(programId){
            case 0:
                return "InsideRx";

            case 1:
                return "U.S Pharmacy Card";

            case 2:
                return "WellRx";

            case 3:
                return "MedImpact";

            case 4:
                return "Singlecare";

            case 5:
                return "Blink";

            case 6:
                return "GoodRx";

            default:
                return "InsideRx";

        }
    }

    private String getAverage(List<Price> prices) {
        return "0.0";
    }

    //When Track-Listing is clicked
    @PostMapping("/addDrugToDashBoard")
    public void addDrugToDashBoard(@RequestBody RequestObject requestObject) throws Throwable {

        if (flag) {
            flag = false;
            setScheduledFutureJob();
        }

        MongoEntity latestAPIResponse = getFinalDrug(requestObject);
        // masterListTestController.addDrugToMasterList(latestAPIResponse);
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

    public MongoEntity getFinalDrug(RequestObject requestObject) throws Throwable {
        long start = System.currentTimeMillis();
        Map<String, String> longitudeLatitude = constructLongLat(requestObject.getZipcode());

        start = System.currentTimeMillis();
        String brandType = getBrandIndicator(requestObject).intern();

        start = System.currentTimeMillis();
        if (brandType.isEmpty()) {
            brandType = B;
            requestObject.setDrugType(BRAND_WITH_GENERIC);
        } else {
            requestObject.setDrugType(brandType.equalsIgnoreCase(G) ? GENERIC : BRAND_WITH_GENERIC);
        }

        start = System.currentTimeMillis();
        CompletableFuture<Blink> blinkFuture = null;
        //Future result

        CompletableFuture<List<InsideRx>> inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
        CompletableFuture<List<DrugNAP2>> usPharmacy = apiService2.constructUsPharmacy(requestObject);
        CompletableFuture<List<Drugs>> wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
        CompletableFuture<LocatedDrug> medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
        CompletableFuture<PharmacyPricings> singleCareFuture = apiService.getSinglecarePrices(requestObject);
        CompletableFuture<GoodRxResponse> goodRxFuture = apiService.getGoodRxPrices(requestObject);

        blinkFuture = apiService3.getBlinkPharmacyPrice(requestObject);


        //Wait until they are all done
        if (blinkFuture != null)
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture, blinkFuture).join();
        else {
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture).join();
            //   start = System.currentTimeMillis();
        }


        //List and obj to store future result
        List<InsideRx> insideRxPrices = inside.get();
        List<DrugNAP2> usPharmacyPrices = usPharmacy.get();
        List<Drugs> wellRx = wellRxFuture.get();
        if (requestObject.getDrugName().equalsIgnoreCase("Genotropin") && requestObject.getDosageStrength().contains("1.6")) {
            System.out.println("GENOTROPIN 1.6");
            try {
                System.out.println(wellRx.get(0).getPrice());
            } catch (Exception ex) {
                System.out.println("ERROR");
            }

        }
        LocatedDrug locatedDrug = medImpactFuture.get();
        PharmacyPricings singleCarePrice = singleCareFuture.get();
        Blink blink = null;
        if (blinkFuture != null)
            blink = apiService3.getBlinkPharmacyPrice(requestObject).get();


        start = System.currentTimeMillis();
        MongoEntity entity = constructEntity(usPharmacyPrices, insideRxPrices, requestObject, wellRx, locatedDrug, singleCarePrice, blink);//BADDDDDD
        MongoEntity newEntity = new MongoEntity();


        start = System.currentTimeMillis();
        MongoEntity m = updateDiff(entity, requestObject);

//        System.out.println("WELL RX PRICE "+m.getPrograms().get(2).getPrice());

        return m;

    }
    public void  createDrugRequests(RequestObject requestObject){
        Map<String, String> longitudeLatitude = constructLongLat(requestObject.getZipcode());
        String brandType = getBrandIndicator(requestObject).intern();


        try {
            CompletableFuture<List<InsideRx>> inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CompletableFuture<List<DrugNAP2>> usPharmacy = apiService2.constructUsPharmacy(requestObject);
        CompletableFuture<List<Drugs>> wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
        CompletableFuture<LocatedDrug> medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
        CompletableFuture<PharmacyPricings> singleCareFuture = apiService.getSinglecarePrices(requestObject);
        CompletableFuture<GoodRxResponse> goodRxFuture = apiService.getGoodRxPrices(requestObject);

    }


    private String getDrugInfoFromInsideRx(WebClient webClient) {
        return webClient
                .get()
                .header("csrf-token", CSRF_TOKEN)
                .header("Cookie", COOKIE)
                .retrieve().bodyToMono(String.class)
                .block();
    }


    public String getBrandIndicator(RequestObject requestObject) {
        List<DrugBrandInfo> drugBrandInfos = getDrugInfoFromUs(requestObject.getDrugName().replace("Hydrochloride", "HCL"));
        StringBuilder sb = new StringBuilder();
        drugBrandInfos.forEach(brand -> {

            if (brand.getProductName().equalsIgnoreCase(requestObject.getDrugName())) {
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
            long start = System.currentTimeMillis();
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
            name.replace("/", "%2F");
            WebClient webClient = WebClient.create("https://api.uspharmacycard.com/drug/lookup/name/" + name.replace("/", "%2F"));
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

    private String convertName(String name) {
        name.replace("/", "%2F");
        return name;
    }

    public MongoEntity constructEntity
            (List<DrugNAP2> usCardProgramResult, List<InsideRx> insideRxProgramResult,
             RequestObject reqObject, List<Drugs> wellRxProgramResult, LocatedDrug medImpactLocatedDrug,
             PharmacyPricings singlecarePrice, Blink blink) {

        MongoEntity finalDrugObject = new MongoEntity();
        Double sum = 0.0;
        String recommended;
        SortedSet<Double> recommendedPriceSet = new TreeSet<>();

        List<Programs> programs = new ArrayList<>();
        finalDrugObject.setName(reqObject.getDrugName());

        if (!CollectionUtils.isEmpty(insideRxProgramResult.get(0).getPrices())) {
            List<Program> programsList = new ArrayList<>();
            programsList.add(new Program("insideRx", insideRxProgramResult.get(0).getPrices().get(0).getPharmacy().getName().toUpperCase(), insideRxProgramResult.get(0).getPrices().get(0).getPrice(), "0.0", "0.0"));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

            recommendedPriceSet.add(Double.parseDouble(insideRxProgramResult.get(0).getPrices().get(0).getPrice()));
        } else {
            List<Program> programsList = new ArrayList<>();
            programsList.add(new Program("insideRx", NA, NA, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }
        try {
            if (!CollectionUtils.isEmpty(usCardProgramResult.get(0).getPriceList())) {
                List<Program> programsList = new ArrayList<>();
                programsList.add(new Program("usPharmacyCard", usCardProgramResult.get(0).getPriceList().get(0).getPharmacy().getPharmacyName().toUpperCase(), usCardProgramResult.get(0).getPriceList().get(0).getDiscountPrice(), "0.0", "0.0"));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);

                recommendedPriceSet.add(Double.parseDouble(usCardProgramResult.get(0).getPriceList().get(0).getDiscountPrice()));
            } else {
                List<Program> programsList = new ArrayList<>();
                programsList.add(new Program("usPharmacyCard", NA, NA, ZERO, ZERO));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);

            }
        } catch (Exception ex) {
            List<Program> programsList = new ArrayList<>();
            programsList.add(new Program("usPharmacyCard", NA, NA, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }
        if (!CollectionUtils.isEmpty(wellRxProgramResult)) {
            List<Program> programsList = new ArrayList<>();
            programsList.add(new Program("wellRx", wellRxProgramResult.get(0).getPharmacyName().toUpperCase(), wellRxProgramResult.get(0).getPrice(), "0.0", "0.0"));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);
            recommendedPriceSet.add(Double.parseDouble(wellRxProgramResult.get(0).getPrice()));
        } else {
            List<Program> programsList = new ArrayList<>();
            programsList.add(new Program("wellRx", NA, NA, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }

        if (medImpactLocatedDrug != null) {
            if (medImpactLocatedDrug.getPricing() != null && medImpactLocatedDrug.getPharmacy() != null) {
                recommendedPriceSet.add(Double.parseDouble(medImpactLocatedDrug.getPricing().getPrice()));
                List<Program> programsList = new ArrayList<>();
                programsList.add(new Program("medImpact", medImpactLocatedDrug.getPharmacy().getName().toUpperCase(), medImpactLocatedDrug.getPricing().getPrice(), "0.0", "0.0"));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);

            } else {
                List<Program> programsList = new ArrayList<>();
                programsList.add(new Program("medImpact", NA, NA, ZERO, ZERO));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);


            }
        } else {
            List<Program> programsList = new ArrayList<>();
            programsList.add(new Program("medImpact", NA, NA, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }

        if (singlecarePrice != null) {
            if (!CollectionUtils.isEmpty(singlecarePrice.getPrices()) && singlecarePrice.getPharmacy() != null) {
                recommendedPriceSet.add(Double.parseDouble(singlecarePrice.getPrices().get(0).getPrice()));
                List<Program> programsList = new ArrayList<>();
                programsList.add(new Program("singlecare", singlecarePrice.getPharmacy().getName().toUpperCase(), singlecarePrice.getPrices().get(0).getPrice(), "0.0", "0.0"));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);
            } else {
                List<Program> programsList = new ArrayList<>();
                programsList.add(new Program("singlecare", NA, NA, ZERO, ZERO));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);
            }
        } else {
            List<Program> programsList = new ArrayList<>();
            programsList.add(new Program("singlecare", NA, NA, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);

        }

        if (blink != null) {
            if (blink.getPrice() != null && blink.getResults() != null) {
                try {
                    recommendedPriceSet.add(Double.parseDouble(blink.getPrice().getLocal().getRaw_value()));
                    List<Program> programsList = new ArrayList<>();
                    programsList.add(new Program("blink", blink.getResults().getName(), blink.getPrice().getLocal().getRaw_value(), ZERO, ZERO));
                    Programs p = new Programs();
                    p.setPrices(programsList);
                    programs.add(p);
                } catch (NullPointerException e) {
                    List<Program> programsList = new ArrayList<>();
                    programsList.add(new Program("blink", NA, NA, ZERO, ZERO));
                    Programs p = new Programs();
                    p.setPrices(programsList);
                    programs.add(p);


                }
            } else {
                List<Program> programsList = new ArrayList<>();
                programsList.add(new Program("blink", NA, NA, ZERO, ZERO));
                Programs p = new Programs();
                p.setPrices(programsList);
                programs.add(p);

            }
        } else {
            List<Program> programsList = new ArrayList<>();
            programsList.add(new Program("blink", NA, NA, ZERO, ZERO));
            Programs p = new Programs();
            p.setPrices(programsList);
            programs.add(p);
        }
        List<Program> programsList = new ArrayList<>();
        programsList.add(new Program("GoodRx", "Coming soon", NA, ZERO, ZERO));
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
        if (usCardProgramResult.size() >= 1) {
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


    public MongoEntity updateDiff(MongoEntity currentObject, RequestObject requestObject) {

        Optional<MongoEntity> mongoObjects = mongoEntityRepo.findByNdcAndDosageStrengthAndQuantityAndZipcode(requestObject.getDrugNDC(),
                requestObject.getDosageStrength().toUpperCase().replaceAll("[MG|MCG|ML|MG-MCG|%]", "").trim(), String.valueOf(requestObject.getQuantity()), requestObject.getZipcode());

        if (mongoObjects.isPresent()) {

            Double aveDiff = 0.0, recommendedDiff = 0.0;

            if (!currentObject.getAverage().equalsIgnoreCase(NA))
                aveDiff = (Double.parseDouble(mongoObjects.get().getAverage()) - Double.parseDouble(currentObject.getAverage()));

            if (!currentObject.getRecommendedPrice().equalsIgnoreCase(NA))
                recommendedDiff = Double.parseDouble(currentObject.getRecommendedPrice()) - (Double.parseDouble(currentObject.getPrograms().get(0).getPrices().get(0).getPrice()));

            currentObject.setAverageDiff(String.valueOf(aveDiff));

            currentObject.setRecommendedDiff(String.valueOf(recommendedDiff));

            if (!CollectionUtils.isEmpty(mongoObjects.get().getPrograms()) && !CollectionUtils.isEmpty(currentObject.getPrograms())) {
                List<Programs> mongoPrograms = mongoObjects.get().getPrograms();
                List<Programs> currPrograms = currentObject.getPrograms();

                mongoPrograms.forEach(mongo -> {

                    for (Programs curr : currPrograms) {

                        if (curr.getPrices().get(0).getProgram().equalsIgnoreCase(mongo.getPrices().get(0).getProgram())) {

                            if (!curr.getPrices().get(0).getPrice().equalsIgnoreCase(NA) && !curr.getPrices().get(0).getPrice().isEmpty()) {

                                Double diff, sum;
                                if (!mongo.getPrices().get(0).getPrice().contains(NA)) {

                                    diff = ((Double.parseDouble(mongo.getPrices().get(0).getPrice())) - Double.parseDouble(curr.getPrices().get(0).getPrice()));
                                    sum = Double.parseDouble(mongo.getPrices().get(0).getPrice()) + Double.parseDouble(curr.getPrices().get(0).getPrice());
                                    double p = (diff / (sum / 2)) * 100d;
                                    curr.getPrices().get(0).setDiff(String.valueOf(diff));
                                    curr.getPrices().get(0).setDiffPerc(String.valueOf(p));

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
