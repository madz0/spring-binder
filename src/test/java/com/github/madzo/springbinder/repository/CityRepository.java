package com.github.madzo.springbinder.repository;

import com.github.madz0.springbinder.repository.BaseRepository;
import com.github.madzo.springbinder.model.City;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends BaseRepository<City, Long> {

}
