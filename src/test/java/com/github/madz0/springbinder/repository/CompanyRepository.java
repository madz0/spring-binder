package com.github.madz0.springbinder.repository;

import com.github.madz0.springbinder.model.Company;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends BaseRepository<Company, Long> {

}
