package com.galaxe.drugpriceapi.web.nap.masterList;

import com.galaxe.drugpriceapi.repositories.MongoEntityRepository;
import com.galaxe.drugpriceapi.web.nap.controller.PriceController;
import com.galaxe.drugpriceapi.web.nap.model.RequestObject;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
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

    @GetMapping("/masterList/addToMasterList")
    public MasterList addToMasterList(){

        try {
            return masterListService.add();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }




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
    @GetMapping("/masterList/getLast")
    public MasterList getLastMasterList(){

        return masterListService.getLastMasterList();
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
    @GetMapping("/masterList/getBetweenTime")
    public List<MasterList> getMasterListsBetweenTime(){

        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date start = cal.getTime();
        Date end = new Date();

        return masterListService.getMasterListsBetweenTime(start,end);
    }
    @GetMapping("/masterList/getBatches")
    public List<MasterList> getAllBatches(){
        return masterListService.getAllBatches();
    }

}
