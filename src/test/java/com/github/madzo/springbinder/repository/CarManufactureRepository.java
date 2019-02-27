package com.github.madzo.springbinder.repository;

import com.github.madzo.springbinder.model.CarManufacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarManufactureRepository extends JpaRepository<CarManufacture, Long> {

}
