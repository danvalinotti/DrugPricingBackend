package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.model.Drug;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Dashboard;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugMasterRepository extends JpaRepository<DrugMaster,Integer> {

    @Query(value= "SELECT * FROM drug_master "+
            "WHERE drug_master.ndc = ?1 AND drug_master.quantity = ?2",nativeQuery = true)
    List<DrugMaster> findAllByFields(String ndc, double quantity);
}
