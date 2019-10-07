package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report,Integer> {


    Report findFirstByOrderByTimestampDesc();



    @Query(value = "SELECT * " +
            " FROM report_table " +
            " WHERE timestamp >= ?1 AND timestamp <= ?2 "  , nativeQuery = true)
    List<Report> findByBetweenDates(Date start, Date end);

    List<Report> findByDrugCount(Integer drugCount);

}
