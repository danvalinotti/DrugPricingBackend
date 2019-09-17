package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugMaster;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.ProgramMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<ProgramMaster,Integer> {
    @Query(value="SELECT program_master.id, program_master.name,program_master.isactive  FROM program_master WHERE program_master.name = ?1 LIMIT 1", nativeQuery = true)
    List<ProgramMaster> findByName(String name);
}
