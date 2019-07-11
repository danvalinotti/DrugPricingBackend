package com.galaxe.drugpriceapi.web.nap.masterList;

import com.galaxe.drugpriceapi.model.ManualReportRequest;
import com.galaxe.drugpriceapi.repositories.MongoEntityRepository;
import com.galaxe.drugpriceapi.web.nap.controller.PriceController;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.DrugMasterController;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.ReportRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Price;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import com.mongodb.Mongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
public class MasterListTestController {
    @Autowired
    MasterListService masterListService;

    @Autowired
    MongoEntityRepository mongoEntityRepo;

    @Autowired
    MasterListRepository masterListRepository;

    @Autowired
    PriceController priceController;
    @Autowired
    DrugMasterController drugMasterController;
    @Autowired
    ReportRepository reportRepository;

    @GetMapping("/masterList/addToMasterList")
    public MasterList addToMasterList(){
        int here = 0;
        try{
            if(reportRepository.count() != 0){
                here= 1;
                drugMasterController.generateReport();
            }else{
                here= 1;
                try {
                    masterListService.add();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

            }
        }catch (Exception e ){
            if(here != 1){
                try {
                    masterListService.add();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }

//        try {
//            MasterList result  = masterListService.add();
//            return result;
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//            return null;
//        }
        return null;
    }
//    @GetMapping("/masterList/getDrugFromMasterList")
//    public List<Price> getDrugFromList(){
//       MongoEntity mongoEntity = getLastMasterList().getDrug().get(0);
//        for (Price price :drugMasterController.mongoEntityToPrices(mongoEntity)) {
//            drugMasterController.addPrice(price);
//        }
//        return drugMasterController.getAllPrices();
//    }

    @GetMapping("/masterList/getByDate/{date}")
    public List<MasterList> addToMasterList(@PathVariable String date){
        final Calendar calendar =  Calendar.getInstance();
        String day = date.substring(3,5) ;
        String month= date.substring(0,2);
        String year =  date.substring(6);
        Integer i =Integer.parseInt(month)-1;
        calendar.set(Integer.parseInt(year),i,Integer.parseInt(day),0,0);
        Date start = calendar.getTime();
        calendar.add(Calendar.DATE, +1);
        Date end = calendar.getTime();

        return masterListService.getMasterListsBetweenTime(start,end);
    }
    @PostMapping("/masterList/addDrugToCurrent")
    public void addDrugToMasterList(@RequestBody MongoEntity requestObject) throws Throwable {

        MongoEntity newDrug = requestObject;
        MasterList lastMasterList = this.masterListService.getLastMasterList();
        lastMasterList.drug.add(newDrug);
        this.masterListRepository.save(lastMasterList);

    }

    @PostMapping("/masterList/manualReport")
    public List<List<String>> createManualReport(@RequestBody ManualReportRequest requestObject) throws Throwable {
       List<List<String>> result = this.masterListService.createManualReport(requestObject);
        System.out.println("Result");
        System.out.println(result);
       return result;

    }
    @GetMapping("/masterList/getLast")
    public MasterList getLastMasterList(){

        return masterListService.getLastMasterList();
    }
    @GetMapping("/masterList/getNumberOfDrugs/{drugCount}")
    public List<MasterList> getReportsWithNumberOfDrugs(@PathVariable int drugCount){

        return masterListService.getReportsWithNumberOfDrugs(drugCount);
    }
    @GetMapping("/masterList/getNumberOfDrugs2/{start}/{end}")
    public List<MasterList> getReportsByNumberOfDrugsRange(@PathVariable int start, @PathVariable int end){

        return masterListService.getReportsByNumberOfDrugsRange(start, end);
    }

    @GetMapping("/masterList/getAll")
    public List<MasterList> getAllMasterLists(){

        return masterListService.getAllMasterLists();
    }
    @GetMapping("/masterList/getById/{id}")
    public MasterList getById(@PathVariable String id){

        return masterListService.getMasterListById(id);
    }
    @GetMapping("/masterList/getByBatch/{batchNum}")
    public MasterList getMasterListByBatch(@PathVariable int batchNum){

        return masterListService.getMasterListByBatch(batchNum);
    }
    @GetMapping("/masterList/getBetweenTime/{startDate}/{endDate}")
    public List<MasterList> getMasterListsBetweenTime(@PathVariable String startDate, @PathVariable String endDate){
        final Calendar calendar =  Calendar.getInstance();
        String day = startDate.substring(3,5) ;
        String month= startDate.substring(0,2);
        String year =  startDate.substring(6);
        Integer i =Integer.parseInt(month)-1;
        calendar.set(Integer.parseInt(year),i,Integer.parseInt(day),0,0);
        Date start = calendar.getTime();

        String day2 = endDate.substring(3,5) ;
        String month2= endDate.substring(0,2);
        String year2 =  endDate.substring(6);
        Integer i2 =Integer.parseInt(month)-1;
        calendar.set(Integer.parseInt(year2),i2,Integer.parseInt(day2),0,0);
        Date end = calendar.getTime();

        return masterListService.getMasterListsBetweenTime(start,end);
    }
    @GetMapping("/masterList/getBatches")
    public List<MasterList> getAllBatches(){
        return masterListService.getAllBatches();
    }

}
