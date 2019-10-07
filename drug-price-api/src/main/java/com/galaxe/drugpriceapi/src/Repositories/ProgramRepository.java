package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.ProgramMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<ProgramMaster,Integer> {

}
