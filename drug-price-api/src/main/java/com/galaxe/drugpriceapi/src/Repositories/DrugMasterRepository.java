package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.DrugMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugMasterRepository extends JpaRepository<DrugMaster,Integer> {

    @Query(value = "SELECT * FROM drug_master "
            + "WHERE drug_master.ndc = ?1 LIMIT 1", nativeQuery = true)
    DrugMaster findByNdc(String ndc);

    @Query(value= "SELECT * FROM drug_master "+
            "WHERE drug_master.ndc = ?1 AND drug_master.quantity = ?2 AND drug_master.zip_code= ?3 ",nativeQuery = true)
    List<DrugMaster> findAllByFields(String ndc, double quantity, String zipcode);

  @Query(value= "SELECT * FROM drug_master "+
            "WHERE drug_master.ndc = ?1 AND drug_master.quantity = ?2 ",nativeQuery = true)
    List<DrugMaster> findAllByNDCQuantity(String ndc, double quantity);

    @Query(value = "WITH summary AS ( " +
            " SELECT *, " +
            " ROW_NUMBER() OVER(PARTITION BY d.ndc, d.quantity " +
            " ORDER BY d.zip_code DESC) AS rk " +
            " FROM drug_master d) " +
            " SELECT s.id, s.dosage_strength, s.dosageuom, s.drug_type,s.gsn, s.name, s.quantity, " +
            " s.report_flag, s.zip_code, s.ndc " +
            "  FROM summary s " +
            " WHERE s.rk = 1"
            , nativeQuery = true)
    List<DrugMaster> getAllWithoutZipCode();

    List<DrugMaster> findByReportFlagOrderById(boolean b);

    DrugMaster getById(Integer id);
}
