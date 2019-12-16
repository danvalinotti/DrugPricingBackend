package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.ReportDrugMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportDrugMasterRepository extends JpaRepository<ReportDrugMaster, Integer> {
    List<ReportDrugMaster> findAllByDrugId(Integer drugId);

    List<ReportDrugMaster> findAllByUserId(Integer userId);

    List<ReportDrugMaster> findAllByUserIdAndDrugId(Integer userId, Integer drugId);

    @Query(value = "SELECT * FROM report_dm " +
            "WHERE user_id = ?1 " +
            "AND schedule like ?2", nativeQuery = true)
    List<ReportDrugMaster> findAllByUserIdAndDate(Integer userId, String date);
}
