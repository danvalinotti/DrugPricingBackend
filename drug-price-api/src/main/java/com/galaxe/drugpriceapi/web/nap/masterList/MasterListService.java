package com.galaxe.drugpriceapi.web.nap.masterList;

import com.galaxe.drugpriceapi.repositories.MongoEntityRepository;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
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
    MongoEntityRepository mongoEntityRepo;
    @Autowired
    MongoTemplate mongoTemplate;
    public MasterList add()
    {
        MasterList m = new MasterList();
        List<MongoEntity> records = mongoEntityRepo.findAll();
        int count = (int)masterListRepository.count();
        addDifference(records, getMasterListByBatch(count));
        m.setDrug(records);
        m.setBatchDetails(new BatchDetails(count+1,new Date()));
        m.setTotalBatches(count+1);
        return masterListRepository.save(m);
    }
    private void addDifference(List<MongoEntity> records, MasterList oldMasterList) {
        for (MongoEntity record:records) {
            MongoEntity oldRecord = oldMasterList.getDrugByNDC(record.getNdc(),record.getQuantity(),record.getZipcode());

            for (int i = 0;i < record.getPrograms().size();i++) {
                try {
                    Double newPrice = Double.parseDouble(record.getPrograms().get(i).getPrice());
                    Double oldPrice = Double.parseDouble(oldRecord.getPrograms().get(i).getPrice());

                    Double diff = newPrice-oldPrice;
                    record.getPrograms().get(i).setDiff(diff.toString());
                }catch (NumberFormatException e){
                    record.getPrograms().get(i).setDiff("0.00");
                }
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


}
