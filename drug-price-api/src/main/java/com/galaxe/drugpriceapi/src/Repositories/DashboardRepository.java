package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard,Integer> {

    @Query(value="SELECT DISTINCT drug_master_id from dashboard WHERE user_id = ?1" , nativeQuery = true)
    List<String> findDistinctDrugsByUserId(int i);


    List<Dashboard> findByDrugMasterId(int i);
}
