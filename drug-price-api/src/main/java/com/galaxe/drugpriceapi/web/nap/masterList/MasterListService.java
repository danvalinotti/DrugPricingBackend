package com.galaxe.drugpriceapi.web.nap.masterList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MasterListService {
    @Autowired
    MasterListRepository masterListRepository ;

    @Autowired
    MongoTemplate mongoTemplate;
    public MasterList add(MasterList masterList){
        return masterListRepository.save(masterList);
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


}
