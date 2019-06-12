package com.galaxe.drugpriceapi.web.nap.masterList;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MasterListRepository extends MongoRepository<MasterList,String> {
    @Query(value = "{'batchDetails.batchNumber' : ?0}")
    MasterList findByBatchNumber(int batchNumber);

    @Query(value = "{ 'batchDetails.batchStart': { $gte: ?0 , $lte: ?1 }}")
    List<MasterList> getMasterListsBetweenTime(Date startTime, Date endTime);

    @Query(value="{}", fields="{ drug : 0 }")
    List<MasterList> getAllBatches();
}
