package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugRequestRepository extends JpaRepository<DrugRequest,Integer> {

    List<DrugRequest> findByDrugIdAndProgramId(int drugId, int i);
}
