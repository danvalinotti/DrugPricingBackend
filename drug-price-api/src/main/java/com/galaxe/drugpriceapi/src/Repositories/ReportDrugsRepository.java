package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.Report_Drugs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportDrugsRepository extends JpaRepository<Report_Drugs,Integer> {


    List<Report_Drugs> findByReportId(int id);

    @Query(value = "SELECT price_id from report_drugs WHERE id = ?1", nativeQuery = true)
    List<Integer> findPriceIdByReportId(int reportId);


}
