package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Dashboard;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard,Integer> {

    List<Dashboard> findByUserId(int profile);

    @Query(value="SELECT DISTINCT drug_master_id from dashboard WHERE user_id = ?1" , nativeQuery = true)
    List<String> findDistinctDrugsByUserId(int i);


    List<Dashboard> findByDrugMasterId(int i);
}
