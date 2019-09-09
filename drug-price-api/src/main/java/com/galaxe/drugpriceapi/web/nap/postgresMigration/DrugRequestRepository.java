package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugRequestRepository extends JpaRepository<DrugRequest,Integer> {

    List<DrugRequest> findByDrugIdAndProgramId(int drugId, Integer i);

    @Query(value = "WITH summary AS ( " +
            " SELECT d.id, d.brand_indicator, d.drug_id, d.drug_name,d.gsn, d.latitude, d.longitude, " +
            "   d.program_id, m.ndc, m.quantity, d.zipcode, d.good_rx_id, " +
            " ROW_NUMBER() OVER(PARTITION BY m.ndc, m.quantity, d.program_id " +
            " ORDER BY d.zipcode DESC) AS rk " +
            " FROM drug_request d FULL OUTER JOIN drug_master m ON m.id = d.drug_id) " +
            " SELECT s.id, s.brand_indicator, s.drug_id, s.drug_name,s.gsn, s.latitude, s.longitude, " +
            " s.ndc, s.program_id, s.quantity, s.zipcode, s.good_rx_id " +
            "  FROM summary s " +
            " WHERE s.rk = 1"
            , nativeQuery = true)
    List<DrugRequest> getAllWithoutZipCode();

    @Query(value = "SELECT * FROM drug_request FULL OUTER JOIN drug_master " +
            "  ON drug_master.id = drug_request.drug_id " +
            "   WHERE drug_master.ndc = ?1 AND drug_master.quantity = ?2 AND drug_request.program_id = ?3"
            , nativeQuery = true)
    List<DrugRequest> findByDrugNDCQuantityAndProgramId(String ndc, double quantity, int programId);

    List<DrugRequest> findByProgramId(int i);
}
