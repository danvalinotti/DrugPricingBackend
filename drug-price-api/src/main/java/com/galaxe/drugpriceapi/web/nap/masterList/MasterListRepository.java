package com.galaxe.drugpriceapi.web.nap.masterList;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MasterListRepository extends MongoRepository<MasterList,String> {
    @Query(value = "{'batchDetails.batchNumber' : ?0}")
    MasterList findByBatchNumber(int batchNumber);

    @Query(value = "{ 'batchDetails.batchStart': { $gte: ?0 , $lte: ?1 }}")
    List<MasterList> getMasterListsBetweenTime(Date startTime, Date endTime);

    @Query(value="{}", fields="{ drug : 0 }")
    List<MasterList> getAllBatches();

    Optional<MasterList> findTopByOrderByTotalBatchesDesc();

    @Query(value = "{'drug' :  {$size: ?0}}")
    List<MasterList> findWithNumberOfDrugs(int drugCount);

    @Query(value = "{'drug' :  {$exists:true}, $where:'this.drug.length>?0'}")
    List<MasterList> getReportsByNumberOfDrugsRange(int start, int end);
}
