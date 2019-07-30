package com.galaxe.drugpriceapi.web.nap.masterList;

import com.galaxe.drugpriceapi.model.*;
import com.galaxe.drugpriceapi.repositories.MongoEntityRepository;
import com.galaxe.drugpriceapi.web.nap.blinkhealth.Blink;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient2;
import com.galaxe.drugpriceapi.web.nap.controller.APIClient3;
import com.galaxe.drugpriceapi.web.nap.controller.PriceController;
import com.galaxe.drugpriceapi.web.nap.medimpact.LocatedDrug;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugMasterController;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugReportController;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Report;
import com.galaxe.drugpriceapi.web.nap.singlecare.PharmacyPricings;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.galaxe.drugpriceapi.web.nap.wellRx.Drugs;
import lombok.AllArgsConstructor;
import org.decimal4j.util.DoubleRounder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import reactor.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class MasterListService {
    @Autowired
    MasterListRepository masterListRepository ;
    @Autowired
    MongoEntityRepository mongoEntityRepo;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    PriceController priceController;
    @Autowired
    APIClient apiService;

    @Autowired
    APIClient2 apiService2;

    @Autowired
    DrugReportController drugReportController;

    @Autowired
    private APIClient3 apiService3;
    private int count = 0;
    private final SortedSet<Double> recommendedPriceSet = new TreeSet<>();

    private final List<String> programs = new ArrayList<>();

    private final String GENERIC = "GENERIC";

    private final String BRAND_WITH_GENERIC = "BRAND_WITH_GENERIC";

    private final String G = "G";

    private final String B = "B";

    private final String NA = "N/A";

    @Autowired
    DrugMasterController drugMasterController;

    private final String ZERO = "0.0";
    public MasterList add() throws Throwable {
        MasterList m = new MasterList();


        List<MongoEntity> records = new ArrayList<>();
        Report report = drugReportController.createFirstReport();

        for (MongoEntity entity: getLastMasterList().drug) {//For each of the drugs in the Master List

           RequestObject r = priceController.constructRequestObjectFromMongo(entity);///mongoEntityRepo.findAll();

            drugReportController.addDrugToReport(r, report);

            //MongoEntity finalDrug = priceController.getFinalDrug(r);

//            records.add(finalDrug);

        }
//        drugMasterController.generateReport();

//
//        addDifference(records, getMasterListByBatch(count)); //eq 116.7
//        m.setDrug(records);
//        int count = (int)masterListRepository.count();
//        m.setBatchDetails(new BatchDetails(count+1,new Date()));
//
//        m.setTotalBatches(count+1);
//        System.out.println("MasterList batch over");
        return m;

    }
    private void addDifference(List<MongoEntity> records, MasterList oldMasterList) {
        for (MongoEntity record:records) {
            try {
                MongoEntity oldRecord = oldMasterList.getDrugByNDC(record.getNdc(), record.getQuantity(), record.getZipcode());

                for (int i = 0; i < record.getPrograms().size(); i++) {
                    try {
                        Double newPrice = Double.parseDouble(record.getPrograms().get(i).getPrice());
                        Double oldPrice = Double.parseDouble(oldRecord.getPrograms().get(i).getPrice());

                        Double diff = newPrice - oldPrice;
                        record.getPrograms().get(i).setDiff(diff.toString());
                    } catch (NumberFormatException e) {
                        record.getPrograms().get(i).setDiff("0.00");
                    } catch (NullPointerException e) {
                        record.getPrograms().get(i).setDiff("0.00");
                    }

                }
            }catch(Exception e ){
            }


        }
    }


    public List<MasterList> getAllMasterLists(){
        return masterListRepository.findAll();
    }
    public MasterList getMasterListById(String id){
        return masterListRepository.findById(id).get();
    }
    public MasterList getMasterListByBatch(int batchNumber){
        return masterListRepository.findByBatchNumber(batchNumber);
    }
    public List<MasterList> getMasterListsBetweenTime(Date startTime, Date endTime){
        return masterListRepository.getMasterListsBetweenTime(startTime,endTime);
    }
    public List<MasterList> getAllBatches(){
        return masterListRepository.getAllBatches();
    }


    public MasterList getLastMasterList() {
        return masterListRepository.findTopByOrderByTotalBatchesDesc().get();
    }

    public List<MasterList> getReportsWithNumberOfDrugs(int drugCount) {
        return masterListRepository.findWithNumberOfDrugs(drugCount);
    }

    public List<MasterList> getReportsByNumberOfDrugsRange(int start, int end) {
        return masterListRepository.getReportsByNumberOfDrugsRange(start , end);
    }

    public List<List<String>> createManualReport(ManualReportRequest requestObject) {
        ArrayList<List<String>> result = new ArrayList<>();
        result.add(getHeader(requestObject));
        for(int i = 0; i<requestObject.getDrugs().size();i++){
            result.add(getDrugData(requestObject.getDrugs().get(i), requestObject));
        }
        return result;
    }

    private List<String> getHeader(ManualReportRequest reportRequest) {
        ArrayList<String> row = new ArrayList<>();

        row.add("Drug Name");

        if(reportRequest.getDrugDetails().contains("Drug Type")){
            row.add("Drug Type");
        }
        if(reportRequest.getDrugDetails().contains("Dosage Strength")){
            row.add("DosageStrength");
        }
        if(reportRequest.getDrugDetails().contains("Quantity")){
            row.add("Quantity");
        }
        if(reportRequest.getDrugDetails().contains("Zip Code")){
            row.add("Zip Code");
        }
        if(reportRequest.getProviders().contains("InsideRx")){
            row.add("InsideRx Price");
        }
        if(reportRequest.getProviders().contains("U.S Pharmacy Card")){
            row.add("U.S Pharmacy Card Price");
        }
        if(reportRequest.getProviders().contains("WellRx")){
            row.add("WellRx Price");
        }
        if(reportRequest.getProviders().contains("MedImpact")){
            row.add("MedImpact Price");
        }
        if(reportRequest.getProviders().contains("SingleCare")){
            row.add("SingleCare Price");
        }
        if(reportRequest.getDrugDetails().contains("Recommended Price")){
            row.add("Recommended Price");
        }
        if(reportRequest.getDrugDetails().contains("Difference")){
            row.add("Difference");
        }
       return row;
    }

    private List<String> getDrugData(MongoEntity mongoEntity, ManualReportRequest reportRequest) {
        ArrayList<String> row = new ArrayList<>();
        row.add(mongoEntity.getName());

        if(reportRequest.getDrugDetails().contains("Drug Type")){
            row.add(mongoEntity.getDrugType());
        }
        if(reportRequest.getDrugDetails().contains("Dosage Strength")){
            row.add(mongoEntity.getDosageStrength());
        }
        if(reportRequest.getDrugDetails().contains("Quantity")){
            row.add(mongoEntity.getQuantity());
        }
        if(reportRequest.getDrugDetails().contains("Zip Code")){
            row.add(mongoEntity.getZipcode());
        }
        RequestObject requestObject = createRequestObject(mongoEntity);

        List<String> prices = null;
        try {
            prices = getFinalDrug(requestObject,reportRequest);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        row.addAll(prices);

        return row;

    }

    private RequestObject createRequestObject(MongoEntity mongoEntity) {
        RequestObject r = new RequestObject();
        r.setDrugType(mongoEntity.getDrugType());
        r.setDosageStrength(mongoEntity.getDosageStrength());
        r.setDrugName(mongoEntity.getName());
        r.setDrugNDC(mongoEntity.getNdc());
        r.setQuantity(Double.parseDouble(mongoEntity.getQuantity()));//WAS PARSE INT
        r.setZipcode(mongoEntity.getZipcode());
        r.setLatitude("latitude");
        r.setLongitude("longitude");
        r.setProgram("program");
        return r;
    }

    public List<String> getFinalDrug(RequestObject requestObject,ManualReportRequest reportRequest) throws Throwable {
//        System.out.println(requestObject.getDrugName());
        long start = System.currentTimeMillis();
        Map<String, String> longitudeLatitude = priceController.constructLongLat(requestObject.getZipcode());

        start = System.currentTimeMillis();
        String brandType = priceController.getBrandIndicator(requestObject).intern();

        start = System.currentTimeMillis();
        if (brandType.isEmpty()) {
            brandType = B;
            requestObject.setDrugType(BRAND_WITH_GENERIC);
        } else {
            requestObject.setDrugType(brandType.equalsIgnoreCase(G) ? GENERIC : BRAND_WITH_GENERIC);
        }

        start = System.currentTimeMillis();
        CompletableFuture<Blink> blinkFuture = null;
        CompletableFuture<List<InsideRx>> inside = new CompletableFuture<>();
        CompletableFuture<List<DrugNAP2>> usPharmacy=new CompletableFuture<>();
        CompletableFuture<List<Drugs>> wellRxFuture =new CompletableFuture<>();
        CompletableFuture<LocatedDrug> medImpactFuture =new CompletableFuture<>();
        CompletableFuture<PharmacyPricings> singleCareFuture = new CompletableFuture<>();
        boolean containsInsideRx=reportRequest.getProviders().contains("InsideRx");
        boolean containsWellRx =reportRequest.getProviders().contains("WellRx");
        boolean containsMedImpact =reportRequest.getProviders().contains("MedImpact");
        boolean containsUSPharmCard =reportRequest.getProviders().contains("U.S Pharmacy Card");
        boolean containsSingleCare =reportRequest.getProviders().contains("SingleCare");

        ArrayList<String> prices= new ArrayList<>();

        //Future result
        if(containsInsideRx){
            inside = apiService.constructInsideRxWebClient(requestObject, longitudeLatitude);

        }
        if(containsUSPharmCard){
            usPharmacy = apiService2.constructUsPharmacy(requestObject);
        }
        if(containsWellRx){
           wellRxFuture = apiService2.getWellRxDrugInfo(requestObject, longitudeLatitude, brandType);
        }
        if(containsMedImpact){
            medImpactFuture = apiService.getMedImpact(requestObject, longitudeLatitude, brandType);
        }
        if(containsSingleCare){
            singleCareFuture = apiService.getSinglecarePrices(requestObject);
        }

        if(containsInsideRx){
            CompletableFuture.allOf(inside).join();
        }
        if(containsUSPharmCard){
            CompletableFuture.allOf(usPharmacy).join();
        }
        if(containsWellRx){
            CompletableFuture.allOf(wellRxFuture).join();
        }
        if(containsMedImpact){
            CompletableFuture.allOf(medImpactFuture).join();
        }
        if(containsSingleCare){
            CompletableFuture.allOf(singleCareFuture).join();
        }



        //Wait until they are all done

        //CompletableFuture.allOf(inside, usPharmacy, wellRxFuture, medImpactFuture, singleCareFuture).join();
        start = System.currentTimeMillis();


        // System.out.println("After all API call done : " + (System.currentTimeMillis() - start));
        //List and obj to store future result
        List<InsideRx> insideRxPrices = null;
        List<DrugNAP2> usPharmacyPrices = null;
        List<Drugs> wellRx = null;
        LocatedDrug locatedDrug =null;
        PharmacyPricings singleCarePrice = null;
        Blink blink = null;
        if(containsInsideRx)
            insideRxPrices = inside.get();
        if(containsUSPharmCard)
            usPharmacyPrices = usPharmacy.get();
        if(containsWellRx)
            wellRx = wellRxFuture.get();
        if(containsMedImpact)
            locatedDrug = medImpactFuture.get();
        if(containsSingleCare)
            singleCarePrice = singleCareFuture.get();



        /*if (blinkFuture != null)
            blink = apiService3.getBlinkPharmacyPrice(requestObject).get();
*/
        start = System.currentTimeMillis();
        List<String> entity = constructEntity(usPharmacyPrices, insideRxPrices, requestObject, wellRx, locatedDrug, singleCarePrice, blink, reportRequest);


        return entity;

    }
    private List<String> constructEntity
            (List<DrugNAP2> usCardProgramResult, List<InsideRx> insideRxProgramResult,
             RequestObject reqObject, List<Drugs> wellRxProgramResult, LocatedDrug medImpactLocatedDrug,
             PharmacyPricings singlecarePrice, Blink blink, ManualReportRequest reportRequest) {

        MongoEntity finalDrugObject = new MongoEntity();
        Double sum = 0.0;
        String recommended;
        recommendedPriceSet.clear();
        programs.clear();
        List<String> providers = reportRequest.getProviders();
        if(providers.contains("InsideRx")) {

            if (!CollectionUtils.isEmpty(insideRxProgramResult.get(0).getPrices())) {
                programs.add( insideRxProgramResult.get(0).getPrices().get(0).getPrice());
                recommendedPriceSet.add(Double.parseDouble(insideRxProgramResult.get(0).getPrices().get(0).getPrice()));
            } else {
                programs.add("0.00");
            }
        }
        if(providers.contains("U.S Pharmacy Card")) {
            if (!CollectionUtils.isEmpty(usCardProgramResult.get(0).getPriceList())) {
                programs.add( usCardProgramResult.get(0).getPriceList().get(0).getDiscountPrice());
                recommendedPriceSet.add(Double.parseDouble(usCardProgramResult.get(0).getPriceList().get(0).getDiscountPrice()));
            } else {
                programs.add("0.00");
            }
        }
        if(providers.contains("WellRx")) {
            if (!CollectionUtils.isEmpty(wellRxProgramResult)) {
                programs.add( wellRxProgramResult.get(0).getPrice());
                recommendedPriceSet.add(Double.parseDouble(wellRxProgramResult.get(0).getPrice()));
            } else {
                programs.add("0.00");
            }
        }
        if(providers.contains("MedImpact")) {
            if (medImpactLocatedDrug != null) {
                if (medImpactLocatedDrug.getPricing() != null && medImpactLocatedDrug.getPharmacy() != null) {
                    recommendedPriceSet.add(Double.parseDouble(medImpactLocatedDrug.getPricing().getPrice()));
                    programs.add( medImpactLocatedDrug.getPricing().getPrice());
                } else {
                    programs.add("0.00");

                }
            } else {
                programs.add("0.00");
            }
        }
        if(providers.contains("SingleCare")) {
            if (singlecarePrice != null) {
                if (!CollectionUtils.isEmpty(singlecarePrice.getPrices()) && singlecarePrice.getPharmacy() != null) {
                    recommendedPriceSet.add(Double.parseDouble(singlecarePrice.getPrices().get(0).getPrice()));
                    programs.add(singlecarePrice.getPrices().get(0).getPrice());
                } else {
                    programs.add("0.00");
                }
            } else {
                programs.add("0.00");
            }
        }

        recommended = String.valueOf(DoubleRounder.round(recommendedPriceSet.first(), 2));
        for (Double p : recommendedPriceSet) {
            sum += p;
        }

        String average = (String.valueOf(DoubleRounder.round(sum / recommendedPriceSet.size(), 2)));

        if(reportRequest.getDrugDetails().contains("Recommended Price")){
            programs.add(recommended);
        }
        if(reportRequest.getDrugDetails().contains("Difference")){
            Double insidePrice = Double.parseDouble(insideRxProgramResult.get(0).getPrices().get(0).getPrice());
            Double recommendedPrice = Double.parseDouble(recommended);
            programs.add((insidePrice-recommendedPrice)+"");
        }




        return programs;

    }
}
