package com.github.madzo.springbinder.repository;

import com.github.madzo.springbinder.model.City;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends CrudRepository<City, Long> {

}
