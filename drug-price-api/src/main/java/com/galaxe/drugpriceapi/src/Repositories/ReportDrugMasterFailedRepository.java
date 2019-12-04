package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.ReportDrugMasterFailed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportDrugMasterFailedRepository extends JpaRepository<ReportDrugMasterFailed, Integer> {
}
