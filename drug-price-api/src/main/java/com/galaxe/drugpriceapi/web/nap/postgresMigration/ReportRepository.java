package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Price;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report,Integer> {


    Report findFirstByOrderByTimestampDesc();

    @Query(value = "SELECT p " +
            " FROM report_drugs RIGHT OUTER JOIN price p " +
            " ON report_drugs.price_id = p.id"  , nativeQuery = true)
    List<Price> getReportPricesById(int id);

    @Query(value = "SELECT * " +
            " FROM report_table " +
            " WHERE timestamp >= ?1 AND timestamp <= ?2 "  , nativeQuery = true)
    List<Report> findByBetweenDates(Date start, Date end);

    @Query(value = "SELECT * " +
            " FROM report_table" +
            " WHERE timestamp = date"  , nativeQuery = true)
    List<Report> findByDate(String date);

    List<Report> findByDrugCount(Integer drugCount);

}
