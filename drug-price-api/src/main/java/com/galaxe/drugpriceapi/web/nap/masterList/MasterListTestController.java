package com.galaxe.drugpriceapi.web.nap.masterList;

import com.galaxe.drugpriceapi.repositories.MongoEntityRepository;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import com.galaxe.drugpriceapi.web.nap.ui.Program;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/masterList/addToMasterList")
    public MasterList addToMasterList(){

        return masterListService.add();
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
