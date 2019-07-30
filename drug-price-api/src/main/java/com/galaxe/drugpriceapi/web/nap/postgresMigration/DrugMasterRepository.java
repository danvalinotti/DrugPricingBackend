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

    List<DrugMaster> findByReportFlag(boolean b);

    @Query(value = "WITH CTE AS( " +
            " SELECT ndc, quantity, dosage_strength, name, " +
            "  rn2 = ROW_NUMBER()OVER(PARTITION BY ndc, quantity, dosage_strength, name ORDER BY name) " +
            " FROM drug_master ) "  +
            "DELETE FROM CTE WHERE rn2 > 1"
            , nativeQuery = true)
    void removeDuplicates();

    @Query(value = "UPDATE drug_master SET report_flag = true;"
            , nativeQuery = true)
    void setAllTrue();

    List<DrugMaster> findByReportFlagOrderById(boolean b);
}
