package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.ReportDrugMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportDrugMasterRepository extends JpaRepository<ReportDrugMaster, Integer> {
    List<ReportDrugMaster> findAllByDrugId(Integer drugId);
}
