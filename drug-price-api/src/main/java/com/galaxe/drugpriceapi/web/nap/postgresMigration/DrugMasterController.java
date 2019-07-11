package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxe.drugpriceapi.model.*;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient2;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient3;
import com.galaxe.drugpriceapi.web.nap.controller.PriceController;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterList;
import com.galaxe.drugpriceapi.web.nap.masterList.MasterListTestController;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.*;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@CrossOrigin
@RestController
public class DrugMasterController {
    @Autowired
    DashboardRepository dashboardRepository;
    @Autowired
    DrugMasterRepository drugMasterRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    ReportDrugsRepository reportDrugsRepository;
    @Autowired
    ProgramRepository programRepository;
    @Autowired
    PriceController priceController;
    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    SavedReportDetailsRepository savedReportDetailsRepository;
    @Autowired
    MasterListTestController masterListTestController;

    @Autowired
    private APIClient apiService;

    @Autowired
    private APIClient2 apiService2;

    @Autowired
    private APIClient3 apiService3;

    int count =0;
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @GetMapping(value = "/report/create")
    public void createReport() throws Throwable {
        Report lastReport = reportRepository.findFirstByOrderByIdDesc();

        Report newReport = new Report();
        newReport.setTimestamp(new Date());
        newReport.setUserId(1);
        newReport.setDrugCount(lastReport.getDrugCount());
        newReport = reportRepository.save(newReport);
        addDrugsToReport(lastReport, newReport);


    }
    @GetMapping(value = "/report/create/empty")
    public void createEmptyReport() throws Throwable {
        Report newReport = new Report();
        newReport.setTimestamp(new Date());
        newReport.setUserId(1);
        newReport.setDrugCount(0);
        newReport.setTimestamp(new Date());
        newReport = reportRepository.save(newReport);
    }

    @GetMapping("/report/create/first")
    public Report createFirstReport() throws Throwable {
        Report report = new Report();
//        Price p = new Price();
//        p = priceRepository.findById(2).get();
        Report_Drugs drugs = new Report_Drugs();
        report.setUserId(1);
        report.setTimestamp(new Date());
        report = reportRepository.save(report);


//        drugs.setReportId(report.getId());
//        drugs.setPriceId(p.getId());
//        drugs = reportDrugsRepository.save(drugs);
        // addDrugsToReport(lastReport, newReport);
        return report;


    }
    @PostMapping("/create/token")
    public Profile createToken(@RequestBody Profile profile){



        ObjectMapper objectMapper = new ObjectMapper();
        String profileJson="";
        try {
           profileJson=  objectMapper.writeValueAsString(profile);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        String jws = Jwts.builder().setSubject(profileJson).signWith(this.key).compact();
        Profile p = new Profile();
        p.setName(jws);
        return p;
//        profile.setUsername(jws);
//        try {
//            String s = Jwts.parser().setSigningKey(key).parseClaimsJws(jws).getBody().getSubject();
//            profile.setPassword(s);
//            return "";
//        }catch (Exception e){
//            profile.setPassword("false");
//            return "";
//        }
       // return p;
    }
    @PostMapping("/authenticate/token")
    public Profile authenticateToken(@RequestBody Profile profile){

        try {
            String s = Jwts.parser().setSigningKey(this.key).parseClaimsJws(profile.getName()).getBody().getSubject();
            ObjectMapper objectMapper = new ObjectMapper();
            Profile p = objectMapper.readValue(s,Profile.class);
            Profile profile1 = profileRepository.findByUsername(p.getUsername()).get(0);
            boolean isCorrect = BCrypt.checkpw(p.getPassword(),profile1.getPassword());

            profile.setPassword(profile.getName());
            profile.setUsername(profile1.getUsername());
            return profile;
        }catch (Exception e){
            profile.setPassword("false");
            return profile;
        }

    }
    @PostMapping("/signUp")
    public Profile signUp(@RequestBody Profile profile){
        String newPassword = BCrypt.hashpw(profile.getPassword(),BCrypt.gensalt());

        profile.setPassword(newPassword);
        return profileRepository.save(profile);
    }


    private void addDrugsToReport(Report lastReport, Report newReport) {
        try {
            List<Price> oldReportPrices = getReportPrices(lastReport);
            List<Price> updatedPrices = updatePrices(oldReportPrices);
            Map<Integer, Double> providerPrices = new HashMap<>();

            Double lowestPrice = updatedPrices.get(0).getPrice();
            Double averagePrice;
            Double sum = 0.0;
            for (Price p2 : updatedPrices) {


                sum = sum + p2.getPrice();
                if (p2.getPrice() <= lowestPrice) {
                    lowestPrice = p2.getPrice();
                }
            }
            averagePrice = sum / updatedPrices.size();

            for (Price p2 : updatedPrices) {
                p2.setRecommendedPrice(lowestPrice);
                p2.setAveragePrice(averagePrice);
                priceRepository.save(p2);
            }

            for (Price p : updatedPrices) {
                Price savedPrice = priceRepository.save(p);
                Report_Drugs report_drugs = new Report_Drugs();
                report_drugs.setPriceId(savedPrice.getId());
                report_drugs.setReportId(newReport.getId());
                reportDrugsRepository.save(report_drugs);
            }
        }catch(Exception ex){

        }

    }

    private List<Price> updatePrices(List<Price> oldReportPrices) {
        List<Price> newPrices = new ArrayList<>();
        for (Price p : oldReportPrices) {
            try {
                newPrices.add(updatePrice(p));
            } catch (Exception e) {

            }

        }

        //  DrugMaster drugMaster = drugMasterRepository.findById(price.getId()).get();
        return newPrices;
    }

    public Price updatePrice(Price price) throws ExecutionException, InterruptedException {

        DrugMaster drugMaster = drugMasterRepository.findById(price.getDrugDetailsId()).get();
        Price newPrice = new Price();
        newPrice.setCreatedat(new Date());
        newPrice.setProgramId(price.getProgramId());
        newPrice.setDrugDetailsId(price.getDrugDetailsId());

        RequestObject requestObject = new RequestObject();
        requestObject.setQuantity(drugMaster.getQuantity());
        requestObject.setDrugType(drugMaster.getDrugType());
        requestObject.setDosageStrength(drugMaster.getDosageStrength());
        requestObject.setDrugName(drugMaster.getName());
        requestObject.setDrugNDC(drugMaster.getNdc());
        requestObject.setZipcode(drugMaster.getZipCode());

        Map<String, String> longitudeLatitude = priceController.constructLongLat(requestObject.getZipcode());
        String brandType = priceController.getBrandIndicator(requestObject).intern();
        if(brandType.equals("B")){
            requestObject.setDrugType("BRAND_WITH_GENERIC");
        }else{
            requestObject.setDrugType("GENERIC");
        }

        if (price.getProgramId() == 0) {
            requestObject.setProgram("insideRx");
            CompletableFuture<List<InsideRx>> inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
            CompletableFuture.allOf(inside).join();
            try {
                List<InsideRx> insideRxPrices = inside.get();
                newPrice.setPrice(Double.parseDouble(insideRxPrices.get(0).getPrices().get(0).getPrice()));
                newPrice.setPharmacy(insideRxPrices.get(0).getPrices().get(0).getPharmacy().getName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 1) {
            requestObject.setProgram("usPharmacyCard");
            CompletableFuture<List<DrugNAP2>> usPharmacy = apiService2.constructUsPharmacy(requestObject);

            CompletableFuture.allOf(usPharmacy).join();
            try {
                List<DrugNAP2> usPharmacyPrices = usPharmacy.get();
                drugMaster = drugMasterRepository.findById(drugMaster.getId()).get();
                drugMaster.setDosageUOM(usPharmacyPrices.get(0).getDosage().getDosageUOM());
                drugMaster = drugMasterRepository.save(drugMaster);
                newPrice.setPrice(Double.parseDouble(usPharmacyPrices.get(0).getPriceList().get(0).getDiscountPrice()));
                newPrice.setPharmacy(usPharmacyPrices.get(0).getPriceList().get(0).getPharmacy().getPharmacyName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 2) {
            CompletableFuture<List<Drugs>> wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
            requestObject.setProgram("wellRx");
            CompletableFuture.allOf(wellRxFuture).join();
            try {
                List<Drugs> wellRx = wellRxFuture.get();
                newPrice.setPrice(Double.parseDouble(wellRx.get(0).getPrice()));
                newPrice.setPharmacy(wellRx.get(0).getPharmacyName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 3) {
            CompletableFuture<LocatedDrug> medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
            requestObject.setProgram("medImpact");
            CompletableFuture.allOf(medImpactFuture).join();
            try {
                LocatedDrug locatedDrug = medImpactFuture.get();
                newPrice.setPrice(Double.parseDouble(locatedDrug.getPricing().getPrice()));
                newPrice.setPharmacy(locatedDrug.getPharmacy().getName());
            } catch (Exception e) {
                return null;
            }

        } else if (price.getProgramId() == 4) {
            CompletableFuture<PharmacyPricings> singleCareFuture = apiService.getSinglecarePrices(requestObject);
            requestObject.setProgram("medImpact");
            CompletableFuture.allOf(singleCareFuture).join();
            try {
                PharmacyPricings singleCarePrice = singleCareFuture.get();

                newPrice.setPrice(Double.parseDouble(singleCarePrice.getPrices().get(0).getPrice()));
                newPrice.setPharmacy(singleCarePrice.getPharmacy().getName());

            } catch (Exception e) {
                return null;
            }
        } else if (price.getProgramId() == 5) {
            CompletableFuture<Blink> blinkFuture = null;
            blinkFuture = apiService3.getBlinkPharmacyPrice(requestObject);

            Blink blink = blinkFuture.get();
            try {
                newPrice.setPrice(Double.parseDouble(blink.getPrice().getLocal().getRaw_value()));
                newPrice.setPharmacy("Blink");
            } catch (Exception e) {
                return null;
            }

        }

        return newPrice;


    }

    private List<Price> getReportPrices(Report lastReport) {
        List<Report_Drugs> report_drugs = new ArrayList<>();
        try {
            report_drugs = reportDrugsRepository.findByReportId(lastReport.getId());
        } catch (Exception e) {

        }
        List<Price> prices = new ArrayList<>();

        for (Report_Drugs report_drug : report_drugs) {

            prices.add(priceRepository.findById(report_drug.getPriceId()).get());
        }
        return prices;
    }



    @PostMapping(value = "/report/drug/add")
    public List<Report_Drugs> addDrugToReport(@RequestBody RequestObject requestObject, Report report2) throws Throwable {

        List<Report_Drugs> report_drugs = new ArrayList<>();
        Map<Integer, Double > providerPrices = new HashMap<>();
        try {
            List<DrugMaster> drugMasterList = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity());
            DrugMaster drugMaster = drugMasterList.get(drugMasterList.size()-1);
            System.out.println("Old Drug");
//            List<Price> prices=  priceRepository.findByDrugDetailsId(drugMaster.getId());
//            System.out.println(drugMaster.getName());
            for (Price p : priceRepository.findByRecentDrugDetails(drugMaster.getId())) {
                System.out.println(p.getProgramId());
                Report_Drugs report_drug = new Report_Drugs();
                Price newPrice = null;
                try {
                    System.out.println("Adding new Price");
                    newPrice = priceRepository.save(updatePrice(p));
                    System.out.println("added new price");
                    report_drug.setPriceId(newPrice.getId());
                    report_drug.setReportId(report2.getId());
                    report_drugs.add(report_drug);
                }catch(Exception exp){

                }
            }
            System.out.println("Prices done ");
            Report report = reportRepository.findById(report2.getId()).get();
            report.setDrugCount(report.getDrugCount() + 1);
            reportRepository.save(report);

            return reportDrugsRepository.saveAll(report_drugs);

        } catch (Exception e) {

            DrugMaster drugMaster = new DrugMaster();

            drugMaster.setQuantity(requestObject.getQuantity());
            drugMaster.setName(requestObject.getDrugName());
            drugMaster.setDosageStrength(requestObject.getDosageStrength());
            drugMaster.setNdc(requestObject.getDrugNDC());
            drugMaster.setDrugType(requestObject.getDrugType());
            drugMaster.setZipCode(requestObject.getZipcode());

            String brandType = priceController.getBrandIndicator(requestObject).intern();
            drugMaster.setDrugType(brandType);
            System.out.println("New Drug");
            System.out.println(drugMaster.getName());

            drugMaster = drugMasterRepository.save(drugMaster);

            List<Price> prices = new ArrayList<>();


            for (int i = 0; i < 6; i++) {
                Price p = new Price();
                p.setDrugDetailsId(drugMaster.getId());
                p.setProgramId(i);
                p.setCreatedat(new Date());
                Price updatedPrice = updatePrice(p);
                if (updatedPrice == null) {

                } else {
                    try {
                        p = priceRepository.save(updatedPrice);
                        providerPrices.put(i, p.getPrice());
                        prices.add(p);
                        Report_Drugs reportDrug = new Report_Drugs();
                        reportDrug.setReportId(report2.getId());
                        reportDrug.setPriceId(p.getId());
                        report_drugs.add(reportDrug);
                    }catch(Exception excep ){

                    }
                }


            }
            Double lowestPrice = providerPrices.get(0);
            Double averagePrice;
            Double sum= 0.0;
            for(Map.Entry<Integer, Double> entry : providerPrices.entrySet()) {
                Integer key = entry.getKey();
                Double value = entry.getValue();
                sum = sum+entry.getValue();
                try {
                    if (entry.getValue() <= lowestPrice) {
                        lowestPrice = entry.getValue();
                    }
                }catch(Exception ex){
                    lowestPrice = entry.getValue();
                }
            }
            averagePrice = sum/ providerPrices.size();

            for(Price p2 : prices){
                p2.setRecommendedPrice(lowestPrice);
                p2.setAveragePrice(averagePrice);
                p2.setCreatedat(new Date());
                priceRepository.save(p2);
            }


        }
        for (Report_Drugs r : report_drugs
        ) {

            r = reportDrugsRepository.save(r);
        }
        Report report = reportRepository.findById(report2.getId()).get();
        report.setDrugCount(report.getDrugCount() + 1);
        reportRepository.save(report);
        if(report_drugs ==null){
            System.out.println("there");
        }
        return report_drugs;

    }


    private Price savePrice(Price p) {
        if (p == null) {
            return null;
        }
        p.setCreatedat(new Date());
        return priceRepository.save(p);
    }

    private DrugMaster saveDrugMaster(DrugMaster drugMaster) {
        return drugMasterRepository.save(drugMaster);
    }

    @GetMapping("/prices/get/all")
    private List<Price> getPrices() {
        return priceRepository.findAll();
    }

    @GetMapping("/drugmaster/get/all")
    private List<DrugMaster> getDrugMasters() {
        return drugMasterRepository.findAll();
    }

    private void saveMongoEntity(MongoEntity mongoEntity) {


    }

    public PricesAndMaster getDetails(RequestObject requestObject, DrugMaster drugMaster) throws Throwable {
        System.out.println(requestObject.getDrugName());
        long start = System.currentTimeMillis();
        Map<String, String> longitudeLatitude = priceController.constructLongLat(requestObject.getZipcode());
        System.out.println("LATLONG:" + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        String brandType = priceController.getBrandIndicator(requestObject).intern();
        System.out.println("GetBrandInd:" + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        if (brandType.isEmpty()) {
            brandType = "B";
            requestObject.setDrugType("BRAND_WITH_GENERIC");
        } else {
            requestObject.setDrugType(brandType.equalsIgnoreCase("G") ? "GENERIC" : "BRAND_WITH_GENERIC");
        }

        start = System.currentTimeMillis();
        CompletableFuture<Blink> blinkFuture = null;
        //Future result
        CompletableFuture<List<InsideRx>> inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
        CompletableFuture<List<DrugNAP2>> usPharmacy = apiService2.constructUsPharmacy(requestObject);
        CompletableFuture<List<Drugs>> wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
        CompletableFuture<LocatedDrug> medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
        CompletableFuture<PharmacyPricings> singleCareFuture = apiService.getSinglecarePrices(requestObject);

        blinkFuture = apiService3.getBlinkPharmacyPrice(requestObject);


        //Wait until they are all done
        if (blinkFuture != null)
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture, blinkFuture).join();
        else {
            CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture).join();
            //    System.out.println("AllProviders:"+(System.currentTimeMillis()-start));
            //   start = System.currentTimeMillis();
        }


        // System.out.println("After all API call done : " + (System.currentTimeMillis() - start));
        //List and obj to store future result
        List<InsideRx> insideRxPrices = inside.get();
        List<DrugNAP2> usPharmacyPrices = usPharmacy.get();
        List<Drugs> wellRx = wellRxFuture.get();
        LocatedDrug locatedDrug = medImpactFuture.get();
        PharmacyPricings singleCarePrice = singleCareFuture.get();
        Blink blink = null;
        if (blinkFuture != null)
            blink = apiService3.getBlinkPharmacyPrice(requestObject).get();

        PricesAndMaster pricesAndMaster = new PricesAndMaster();
        List<Price> prices = new ArrayList<>();


        InsideRx insideRx = insideRxPrices.get(0);
        Price p = new Price();
        try {
            p.setPrice(Double.parseDouble(insideRx.getPrices().get(0).getPrice()));
            p.setPharmacy(insideRx.getPrices().get(0).getPharmacy().getName());
            p.setDrugDetailsId(drugMaster.getId());
            p.setProgramId(0);
        } catch (Exception e) {
            p = null;
        }
        DrugNAP2 usPharm = usPharmacyPrices.get(0);
        Price p1 = new Price();
        try {
            p1.setPrice(Double.parseDouble(usPharm.getPriceList().get(0).getDiscountPrice()));
            p1.setPharmacy(usPharm.getPriceList().get(0).getPharmacy().getPharmacyName());
            p1.setProgramId(1);
            p1.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p1 = null;
        }

        Price p2 = new Price();
        try {
            Drugs well = wellRx.get(0);
            p2.setPrice(Double.parseDouble(well.getPrice()));
            p2.setPharmacy(well.getPharmacyName());
            p2.setProgramId(2);
            p2.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p2 = null;
        }
        //MediIMpact
        Price p3 = new Price();
        try {
            p3.setPrice(Double.parseDouble(locatedDrug.getPricing().getPrice()));
            p3.setPharmacy(locatedDrug.getPharmacy().getName());
            p3.setProgramId(3);
            p3.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p3 = null;
        }
        Price p4 = new Price();
        try {
            p4.setPrice(Double.parseDouble(singleCarePrice.getPrices().get(0).getPrice()));
            p4.setPharmacy(singleCarePrice.getPharmacy().getName());
            p4.setProgramId(4);
            p4.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p4 = null;
        }
        Price p5 = new Price();
        try {
            p5.setPrice(Double.parseDouble(blink.getPrice().getLocal().getRaw_value()));
            p5.setPharmacy(blink.getResults().getName());
            p5.setProgramId(5);
            p5.setDrugDetailsId(drugMaster.getId());
        } catch (Exception e) {
            p5 = null;
        }
        prices.add(p);
        prices.add(p1);
        prices.add(p2);
        prices.add(p3);
        prices.add(p4);
        prices.add(p5);


        pricesAndMaster.setDrugMaster(drugMaster);
        pricesAndMaster.setPrices(prices);

        return pricesAndMaster;
//        start = System.currentTimeMillis();
//        MongoEntity entity =  priceController.constructEntity(usPharmacyPrices, insideRxPrices, requestObject, wellRx, locatedDrug, singleCarePrice, blink);
//        MongoEntity newEntity = new MongoEntity();
//
//        System.out.println("ConstructEntity:"+(System.currentTimeMillis()-start));
//        start = System.currentTimeMillis();
//        MongoEntity m  =  priceController.updateDiff(entity,requestObject);
//        System.out.println("updateDiff:"+(System.currentTimeMillis()-start));


    }

    @GetMapping(value = "/drugs/getAll")
    public List<DrugMaster> getAllDrugs() {
        return null;
    }

    @GetMapping(value = "/dashboard/drugs/get")
    public List<PricesAndMaster> getDashboardDrugs() {
        int profile = 1;
        List<Dashboard> dashboards2 = dashboardRepository.findAll();
        List<Dashboard> dashboards = dashboardRepository.findByUserId(profile);
        List<PricesAndMaster> newList = createPricesAndMasterList(dashboards);

        return newList;

    }
    @PostMapping(value = "/dashboard/drug/delete")
    public void deleteDashboardDrug(@RequestBody MongoEntity mongoEntity) {
        DrugMaster drugMaster = drugMasterRepository.findAllByFields(mongoEntity.getNdc(),Double.parseDouble(mongoEntity.getQuantity())).get(0);
        List<Dashboard> dashboards = dashboardRepository.findByDrugMasterId(drugMaster.getId());
        dashboardRepository.deleteAll(dashboards);
    }

    @PostMapping("/dashboard/get")
    List<MongoEntity> getDashboard(@RequestBody StringSender token) {
        Profile testProfile = new Profile ();
        testProfile.setName(token.getValue());
        String userName = authenticateToken(testProfile).getUsername();
        Profile signedInUser = profileRepository.findByUsername(userName).get(0);
        int userId = signedInUser.getId();
        List<String> drugList = dashboardRepository.findDistinctDrugsByUserId(userId);
        List<DrugMaster> drugMasters = new ArrayList<>();
        List<MongoEntity> mongoEntities = new ArrayList<>();

        for (String s : drugList) {
            DrugMaster drugMaster = drugMasterRepository.findById(Integer.parseInt(s)).get();
            drugMasters.add(drugMaster);
            MongoEntity mongoEntity = new MongoEntity();

            mongoEntity.setQuantity(drugMaster.getQuantity() + "");
            //mongoEntity.setPharmacyName("");
            mongoEntity.setNdc(drugMaster.getNdc());
            mongoEntity.setDrugType(drugMaster.getDrugType());
            mongoEntity.setDosageStrength(drugMaster.getDosageStrength());
            mongoEntity.setName(drugMaster.getName());
            mongoEntity.setZipcode(drugMaster.getZipCode());

            List<Price> prices = priceRepository.findByDrugDetailsId(drugMaster.getId());
            List<Program> programs = new ArrayList<>();
            mongoEntity.setRecommendedPrice(prices.get(0).getRecommendedPrice()+"");
            mongoEntity.setAverage(prices.get(0).getAveragePrice()+"");
            Program[] programArr = new Program[6];

            for (int i = 0; i < programArr.length; i++) {
                Program program = new Program();
                program.setProgram(i + "");
                program.setPrice("N/A");
                program.setPharmacy("N/A");
                //programs.add(program);
                programArr[i] = program;
            }
            for (Price p : prices) {
                Program program = new Program();
                program.setProgram(p.getProgramId() + "");
                program.setPrice(p.getPrice() + "");
                program.setPharmacy(p.getPharmacy());
                //programs.add(program);
                programArr[p.getProgramId()] = program;
            }
            programs = Arrays.asList(programArr);


            mongoEntity.setPrograms(programs);
            mongoEntities.add(mongoEntity);
        }

        return mongoEntities;
    }


    @PostMapping(value = "/dashboard/drugs/add")
    public Dashboard addDrugToDashboard(@RequestBody RequestObject requestObject) throws Throwable {
        Dashboard d = new Dashboard();
        DrugMaster drugMaster;
        try {
            drugMaster = drugMasterRepository.findAllByFields(requestObject.getDrugNDC(), requestObject.getQuantity()).get(0);
        } catch (Exception e) {
            DrugMaster drugMaster1 = new DrugMaster();
            drugMaster1.setZipCode(requestObject.getZipcode());
            drugMaster1.setDrugType(requestObject.getDrugType());
            drugMaster1.setNdc(requestObject.getDrugNDC());
            drugMaster1.setDosageStrength(requestObject.getDosageStrength());
            drugMaster1.setName(requestObject.getDrugName());
            drugMaster1.setQuantity(requestObject.getQuantity());
            String brandType = priceController.getBrandIndicator(requestObject).intern();
            drugMaster1.setDrugType(brandType);
            drugMaster = drugMasterRepository.save(drugMaster1);
        }


        d.setDrugMasterId(drugMaster.getId());
        Profile testProfile = new Profile();
        testProfile.setName(requestObject.getToken());
        String username = authenticateToken(testProfile).getUsername();
        Profile signedInUser = profileRepository.findByUsername(username).get(0);

        d.setUserId(signedInUser.getId());//hardcoded user
        addPrices(requestObject, drugMaster);
        //reportDrugsRepository.saveAll(addDrugToReport(requestObject));
        return dashboardRepository.save(d);
    }

    public List<Price> addPrices(RequestObject requestObject, DrugMaster drugMaster) throws Throwable {
        PricesAndMaster details = getDetails(requestObject, drugMaster);
        List<Price> addedPrices = new ArrayList<>();

        for (Price price : details.getPrices()) {
            addedPrices.add(savePrice(price));
        }
        Map<Integer,Double> providerPrices = new HashMap<>();

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

    private List<PricesAndMaster> createPricesAndMasterList(List<Dashboard> dashboards) {
        List<PricesAndMaster> pricesAndMasters = new ArrayList<>();

        for (Dashboard d : dashboards) {
            PricesAndMaster pricesAndMaster = new PricesAndMaster();
            DrugMaster drugMaster = drugMasterRepository.findById(d.getDrugMasterId()).get();
            List<Price> prices = priceRepository.findByDrugDetailsId(drugMaster.getId());
            pricesAndMaster.setPrices(prices);
            pricesAndMaster.setDrugMaster(drugMaster);
            pricesAndMasters.add(pricesAndMaster);
        }
        return pricesAndMasters;
    }

    @PostMapping(value = "/create/report/manual")//List<List<String>>
    public ResponseEntity<Resource> createManualReport(@RequestBody ManualReportRequest2 manualrequestObject) {
        //Create rows list
        List<List<String>> rows = new ArrayList<>();
        Profile p1 = new Profile();
           p1.setName(manualrequestObject.getToken());
         Profile user = profileRepository.findByUsername(authenticateToken(p1).getUsername()).get(0);

        if(manualrequestObject.getIsSaved()){
            SavedReportDetails savedReportDetails = new SavedReportDetails();
            savedReportDetails.setDrug_fields(manualrequestObject.getDrugDetails());
            List<Integer> drugIds = new ArrayList<>();

            for (DrugMaster drugMaster: manualrequestObject.getDrugs()) {
                drugIds.add(drugMaster.getId());
            }
            savedReportDetails.setDrug_ids(drugIds);
            savedReportDetails.setName(manualrequestObject.getName());
            savedReportDetails.setProviders(manualrequestObject.getProviders());
            savedReportDetails.setUserId(user.getId());
            savedReportDetailsRepository.save(savedReportDetails);

        }

        rows.add(getHeaders(manualrequestObject));
        for (int i = 0; i < manualrequestObject.getDrugs().size(); i++) {//for all of the drugs
            List<String> row = new ArrayList<>();
            DrugMaster drugMaster = drugMasterRepository.findById(manualrequestObject.getDrugs().get(i).getId()).get();


            row.add(drugMaster.getName());

            RequestObject requestObject = new RequestObject();
            requestObject.setQuantity(drugMaster.getQuantity());
            requestObject.setDrugType(drugMaster.getDrugType());
            requestObject.setDosageStrength(drugMaster.getDosageStrength());
            requestObject.setDrugName(drugMaster.getName());
            requestObject.setDrugNDC(drugMaster.getNdc());
            requestObject.setZipcode(drugMaster.getZipCode());

            Map<String, String> longitudeLatitude = priceController.constructLongLat(requestObject.getZipcode());

            if (manualrequestObject.getDrugDetails().contains("Brand Type")) {
                row.add(drugMaster.getDrugType());
            }
            if (manualrequestObject.getDrugDetails().contains("Dosage Strength")) {
                row.add(drugMaster.getDosageStrength());
            }
            if (manualrequestObject.getDrugDetails().contains("Quantity")) {
                row.add(drugMaster.getQuantity() + "");
            }
            if (manualrequestObject.getDrugDetails().contains("Zip Code")) {
                row.add(drugMaster.getZipCode());
            }
            if (manualrequestObject.getDrugDetails().contains("Recommended Price")) {
                row.add(priceRepository.findByDrugDetailsId(drugMaster.getId()).get(0).getRecommendedPrice()+"");
            }
            if (manualrequestObject.getDrugDetails().contains("Difference")) {
                Double recommended = priceRepository.findByDrugDetailsId(drugMaster.getId()).get(0).getRecommendedPrice();
                Double current  =Double.parseDouble(getPharmacyPrice("InsideRx", drugMaster, requestObject, longitudeLatitude));
                row.add(recommended-current +"");
            }
            for(String s : manualrequestObject.getProviders()){
                row.add(getPharmacyPrice(s, drugMaster, requestObject, longitudeLatitude));
            }
//            if (manualrequestObject.getProviders().contains("InsideRx")) {
//                row.add(getPharmacyPrice("InsideRx", drugMaster, requestObject, longitudeLatitude));
//            }
//            if (manualrequestObject.getProviders().contains("WellRx")) {
//                row.add(getPharmacyPrice("WellRx", drugMaster, requestObject, longitudeLatitude));
//            }
//            if (manualrequestObject.getProviders().contains("SingleCare")) {
//                row.add(getPharmacyPrice("SingleCare", drugMaster, requestObject, longitudeLatitude));
//            }
//            if (manualrequestObject.getProviders().contains("MedImpact")) {
//                row.add(getPharmacyPrice("MedImpact", drugMaster, requestObject, longitudeLatitude));
//            }
//            if (manualrequestObject.getProviders().contains("U.S Pharmacy Card")) {
//                row.add(getPharmacyPrice("U.S Pharmacy Card", drugMaster, requestObject, longitudeLatitude));
//            }
            rows.add(row);
        }

       return  exportManualReport(rows);
     //   return rows;
    }
    public ResponseEntity<Resource> exportManualReport (List<List<String>>  rows){
        Workbook workbook = new XSSFWorkbook();

        CreationHelper createHelper = workbook.getCreationHelper();

        Sheet sheet = workbook.createSheet("DrugReport");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
//        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        for(int i = 0; i < rows.get(0).size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(rows.get(0).get(i));
            cell.setCellStyle(headerCellStyle);
        }
        int count = 0;
        for (List<String> row : rows) {
            if(count == 0){

            }else{
                Row r = sheet.createRow(count);

                int cellCount = 0;
                for(String cell:row){
                    r.createCell(cellCount).setCellValue(cell);
                    cellCount++;
                }
                cellCount=0;
            }
            count++;
        }
        for(int i = 0; i < rows.get(0).size(); i++) {
            sheet.autoSizeColumn(i);
        }
        FileOutputStream fileOut;
        InputStreamResource resource = null;
        try {
            fileOut = new FileOutputStream("poi-generated-file.xlsx");
            InputStream fileInputStream = new FileInputStream("poi-generated-file.xlsx");
            resource = new InputStreamResource(new FileInputStream("poi-generated-file.xlsx"));
            workbook.write(fileOut);

            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        File file = new File("poi-generated-file.xlsx");
        file.length();
        //we are saying we are getting an attachment and what to name it
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+"poi-generated-file.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
               // .contentLength(resumelength)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(resource);

    }
    @PostMapping("/reports/saved/get")
    public List<SavedReportHelper> getSavedReport(@RequestBody StringSender token){
        Profile p1 = new Profile();
        p1.setName(token.getValue());
        Profile p = authenticateToken(p1);
        p = profileRepository.findByUsername(p.getUsername()).get(0);

        List<SavedReportHelper> savedReportHelper = new ArrayList<>();
        savedReportHelper = convertToReportDrugs(savedReportDetailsRepository.findByUserId(p.getId()));
        return savedReportHelper;
    }

    private List<SavedReportHelper> convertToReportDrugs(List<SavedReportDetails> byUserId) {
        List<SavedReportHelper> drugList = new ArrayList<>();
        for (SavedReportDetails saved: byUserId){
            SavedReportHelper details2 = new SavedReportHelper();
            details2.setDrug_fields(saved.getDrug_fields());

            details2.setName(saved.getName());
            details2.setProviders(saved.getProviders());
            details2.setUserId(saved.getUserId());
            details2.setDrug_ids(drugMasterRepository.findAllById(saved.getDrug_ids()));
            drugList.add(details2);
        }
        return drugList;
    }

    private List<String> getHeaders(ManualReportRequest2 manualrequestObject) {
        List<String> row = new ArrayList<>();
        row.add("Drug Name");
        row.addAll(manualrequestObject.getDrugDetails());
        row.addAll(manualrequestObject.getProviders());

        return row;
    }

    private String getPharmacyPrice(String providerName, DrugMaster drugMaster, RequestObject requestObject, Map<String, String> longitudeLatitude) {

        try {
            String brandType = drugMaster.getDrugType();
            if (providerName.equals("InsideRx")) {
                requestObject.setProgram("insideRx");
                CompletableFuture<List<InsideRx>> inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);
                CompletableFuture.allOf(inside).join();
                List<InsideRx> insideRxPrices = inside.get();

                return insideRxPrices.get(0).getPrices().get(0).getPrice();

            } else if (providerName.equals("U.S Pharmacy Card")) {
                CompletableFuture<List<DrugNAP2>> usPharmacy = apiService2.constructUsPharmacy(requestObject);
                requestObject.setProgram("usPharmacyCard");
          //      requestObject.setDrugType("BRAND_WITH_GENERIC");
                CompletableFuture.allOf(usPharmacy).join();
                try {
                    List<DrugNAP2> usPharmacyPrices = usPharmacy.get();
                    if(drugMaster.getDosageUOM() == null || drugMaster.getDosageUOM().equals("")){
                        drugMaster= drugMasterRepository.findById(drugMaster.getId()).get();
                        drugMaster.setDosageUOM(usPharmacyPrices.get(0).getDosage().getDosageUOM());
                        drugMasterRepository.save(drugMaster);
                    }
                    return usPharmacyPrices.get(0).getPriceList().get(0).getDiscountPrice();
                } catch (Exception e) {
                    return "";
                }

            } else if (providerName.equals("WellRx")) {
                if(brandType.equals("BRAND_WITH_GENERIC")){
                    brandType = "B";
                }else{
                    brandType = "G";
                }
                CompletableFuture<List<Drugs>> wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
                requestObject.setProgram("wellRx");
                CompletableFuture.allOf(wellRxFuture).join();
                List<Drugs> wellRx = wellRxFuture.get();
                return wellRx.get(0).getPrice()+ " ";

            } else if (providerName.equals("MedImpact")) {
//                requestObject.setDrugType("BRAND_WITH_GENERIC");
//                brandType = "B";
                CompletableFuture<LocatedDrug> medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
                requestObject.setProgram("medImpact");

                CompletableFuture.allOf(medImpactFuture).join();
                LocatedDrug locatedDrug = medImpactFuture.get();

                return locatedDrug.getPricing().getPrice();

            } else if (providerName.equals("SingleCare")) {
                CompletableFuture<PharmacyPricings> singleCareFuture = apiService.getSinglecarePrices(requestObject);
                requestObject.setProgram("singleCare");
                CompletableFuture.allOf(singleCareFuture).join();
                PharmacyPricings singleCarePrice = singleCareFuture.get();

                return singleCarePrice.getPrices().get(0).getPrice();

            } else if (providerName.equals("InsideRx")) {
                CompletableFuture<Blink> blinkFuture = null;
                blinkFuture = apiService3.getBlinkPharmacyPrice(requestObject);

                Blink blink = blinkFuture.get();
                try {
                    return blink.getPrice().getLocal().getRaw_value();
                } catch (Exception e) {
                    return "";
                }

            }
            return " ";
        } catch (Exception e) {
            return " ";
        }
    }

    @GetMapping(value = "/report/automatic")
    public Report generateReport() {
        int forward= 0;
        try {
            int reportId = reportRepository.findFirstByOrderByIdDesc().getId();
            Report newReport = new Report();
            newReport.setTimestamp(new Date());
            newReport.setDrugCount(0);
            newReport = reportRepository.save(newReport);
            forward= 1;
            //Get report drugs by  report id
            List<Report_Drugs> report_drugs = reportDrugsRepository.findByReportId(reportId);

            Set<Integer> distinctDrugs = new HashSet<>();

            for (Report_Drugs reportDrug : report_drugs) {
                distinctDrugs.add(priceRepository.findById(reportDrug.getPriceId()).get().getDrugDetailsId());
            }

            List<MongoEntity> mongoEntities = new ArrayList<>();

            //create a mongoEntity from a distinct drug and prices
            for (Integer i : distinctDrugs) {

                DrugMaster drugMaster = drugMasterRepository.findById(i).get();

                RequestObject requestObject = new RequestObject();

                requestObject.setQuantity(drugMaster.getQuantity());
                requestObject.setDrugType(drugMaster.getDrugType());
                requestObject.setDosageStrength(drugMaster.getDosageStrength());
                requestObject.setDrugName(drugMaster.getName());
                requestObject.setDrugNDC(drugMaster.getNdc());
                requestObject.setZipcode(drugMaster.getZipCode());
                Map<String, String> longitudeLatitude = priceController.constructLongLat(requestObject.getZipcode());
                try {
                    addDrugToReport(requestObject, newReport);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }


            }

            return newReport;
        }catch(Exception e){
            if(forward == 0 ) {
                masterListTestController.addToMasterList();
            }
        }
        return null;
    }

    private void addPriceToReport(Price price) {



    }

    //    @GetMapping(value = "/dashboard/drugs/add")
//    public Dashboard addToDashboard(){
//        return null;
//    }
    @GetMapping(value = "/reports/getAll")
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }
    @GetMapping(value = "/reports/get/between/{start}/{end}")
    public List<Report> getReportsBetween(@PathVariable String start , @PathVariable String end) throws ParseException {
        String dt = start;// Start date
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(dt));

        String dt2 = end;// Start date
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd-yyyy");
        Calendar c2 = Calendar.getInstance();
        c2.setTime(sdf2.parse(dt2));

        Date d1 = c.getTime();
        Date d2 = c2.getTime();
        List<Report> r= reportRepository.findByBetweenDates(d1,d2);
        return r;
    }
    @GetMapping(value = "/reports/get/date/{date}")
    public List<Report> getReportsByDate(@PathVariable String date) throws ParseException {
//        Date d =  DateFormat.getDateInstance().parse(date);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(d);
        String dt = date;// Start date
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(dt));

        dt = sdf.format(c.getTime());  // dt is now the new date
        String dt2 = date;// Start date
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd-yyyy");
        Calendar c2 = Calendar.getInstance();
        c2.setTime(sdf.parse(dt));
        c2.add(Calendar.DATE, 1);  // number of days to add
        dt = sdf.format(c.getTime());  // dt is now the new date
        Date d1 = c.getTime();
        Date d2 = c2.getTime();
        return reportRepository.findByBetweenDates(d1, d2 );
    }
    @GetMapping(value = "/reports/get/drugCount/{drugCount}")
    public List<Report> getReportsByDrugCount(@PathVariable String drugCount) {
        Integer drug_count = Integer.parseInt(drugCount);
        return reportRepository.findByDrugCount(drug_count);
    }

    @GetMapping(value = "/reportsdrugs/getAll")
    public List<Report_Drugs> getAllReportDrugs() {
        return reportDrugsRepository.findAll();
    }

    @GetMapping(value = "/reportdrugs/get/{r}")
    public ResponseEntity<Resource> exportReportById(@PathVariable String r) {
        int reportId = Integer.parseInt(r);

        //Get report drugs by  report id
        List<Report_Drugs> report_drugs = reportDrugsRepository.findByReportId(reportId);

        Set<Integer> distinctDrugs = new HashSet<Integer>();

        for (Report_Drugs reportDrug : report_drugs) {
            distinctDrugs.add(priceRepository.findById(reportDrug.getPriceId()).get().getDrugDetailsId());
        }

        List<MongoEntity> mongoEntities = new ArrayList<>();

        //create a mongoEntity from a distinct drug and prices
        for (Integer i : distinctDrugs) {
            MongoEntity mongoEntity = new MongoEntity();
            DrugMaster drugMaster = drugMasterRepository.findById(i).get();
            mongoEntity.setZipcode(drugMaster.getZipCode());
            mongoEntity.setName(drugMaster.getName());
            mongoEntity.setDosageStrength(drugMaster.getDosageStrength());
            mongoEntity.setDrugType(drugMaster.getDrugType());
            mongoEntity.setNdc(drugMaster.getNdc());
            mongoEntity.setQuantity(drugMaster.getQuantity() + "");

            List<Program> programs = new ArrayList<>();
            Program[] programArr = new Program[6];
            List<Price> prices = priceRepository.findByDrugDetailsId(i);
            mongoEntity.setAverage(prices.get(0).getAveragePrice()+"");
            mongoEntity.setRecommendedPrice(prices.get(0).getRecommendedPrice()+"");
            for (int x = 0; x < programArr.length; x++) {
                Program program = new Program();
                program.setProgram(x + "");
                program.setPrice("N/A");
                program.setPharmacy("N/A");
                //programs.add(program);
                programArr[x] = program;
            }
            for (Price p : prices) {
                Program program = new Program();
                program.setProgram(p.getProgramId() + "");
                program.setPrice(p.getPrice() + "");
                program.setPharmacy(p.getPharmacy());
                //programs.add(program);
                programArr[p.getProgramId()] = program;

                if(Integer.parseInt(program.getProgram()) == 0){
                    mongoEntity.setRecommendedDiff(Double.parseDouble(mongoEntity.getRecommendedPrice())- Double.parseDouble(program.getPrice()) +"");
                }
            }
            programs = Arrays.asList(programArr);
            mongoEntity.setPrograms(programs);
            mongoEntities.add(mongoEntity);
        }


        //add to list mongo entity
        return exportReport(mongoEntities);
        //return

    }
   public ResponseEntity<Resource> exportReport(List<MongoEntity> mongoEntities){

        List<List<String>> rows = new ArrayList<>();
        String[] str =  {"Drug Name", "Drug Type", "Dosage Strength",
       "Quantity", "Zip Code", "Inside Rx Price", "U.S Pharmacy Card Price",
       "Well Rx Price", "MedImpact Price", "Singlecare Price",
       "Recommended Price", "Difference"};
        List<String> header = Arrays.asList(str);

        rows.add(header);

        for(MongoEntity element : mongoEntities){
            List<String> row = new ArrayList<>();

            row.add(element.getName());
            row.add(element.getDrugType());
            row.add(element.getDosageStrength());

            row.add(element.getQuantity());
            row.add(element.getZipcode());
            row.add(element.getPrograms().get(0).getPrice());
            row.add(element.getPrograms().get(1).getPrice());
            row.add(element.getPrograms().get(2).getPrice());
            row.add(element.getPrograms().get(3).getPrice());
            row.add(element.getPrograms().get(4).getPrice());
            row.add(element.getRecommendedPrice());
            row.add(element.getRecommendedDiff());
            rows.add(row);
        }

        return exportManualReport(rows);

   }
   /*
   var exportList = [["Drug Name", "Drug Type", "Dosage Strength",
               "Quantity", "Zip Code", "Inside Rx Price", "U.S Pharmacy Card Price",
               "Well Rx Price", "MedImpact Price", "Singlecare Price",
               "Recommended Price", "Difference"]];
       data.forEach((element, index) => {
           var row = [element.name, element.drugType, element.dosageStrength + " " + element.dosageUOM,
                   element.quantity, '= "' + element.zipcode + '"', element.programs[0].price, element.programs[1].price,
                   element.programs[2].price, element.programs[3].price, element.programs[4].price,
                   element.recommendedPrice, element.recommendedDiff];

           exportList.push(row);

       });
    */
}
