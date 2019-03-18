package com.github.madzo.springbinder.repository;

import com.github.madzo.springbinder.model.House;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseRepository extends JpaRepository<House, Long> {
}
