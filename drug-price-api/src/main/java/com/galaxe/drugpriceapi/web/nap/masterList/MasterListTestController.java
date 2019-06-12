package com.galaxe.drugpriceapi.web.nap.masterList;

import com.galaxe.drugpriceapi.repositories.MongoEntityRepository;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class MasterListTestController {
    @Autowired
    MasterListService masterListService;

    @Autowired
    MongoEntityRepository mongoEntityRepo;

    @GetMapping("/masterList/addToMasterList")
    public MasterList addToMasterList(){
        MasterList m = new MasterList();
        List<MongoEntity> records = mongoEntityRepo.findAll();
        m.setDrug(records);
        m.setBatchDetails(new BatchDetails(2,new Date()));
        m.setTotalBatches(2);
        return masterListService.add(m);
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
