package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.DrugRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugRequestRepository extends JpaRepository<DrugRequest,Integer> {

    List<DrugRequest> findByDrugIdAndProgramId(String drugId, Integer i);

    @Query(value = "SELECT * FROM drug_request FULL OUTER JOIN drug_master " +
            "  ON drug_master.id = drug_request.drug_id " +
            "   WHERE drug_master.ndc = ?1 AND drug_master.quantity = ?2 AND drug_request.program_id = ?3"
            , nativeQuery = true)
    List<DrugRequest> findByDrugNDCQuantityAndProgramId(String ndc, double quantity, int programId);

    @Query(value = "SELECT * FROM drug_request WHERE drug_id = ?1 ORDER BY program_id ASC", nativeQuery = true)
    List<DrugRequest> findAllByDrugId(String drugId);

}
