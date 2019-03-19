package com.github.madzo.springbinder.repository;

import com.github.madz0.springbinder.repository.BaseRepository;
import com.github.madzo.springbinder.model.Company;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends BaseRepository<Company, Long> {

}
