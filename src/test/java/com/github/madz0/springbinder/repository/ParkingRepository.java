package com.github.madz0.springbinder.repository;

import com.github.madz0.springbinder.model.Parking;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingRepository extends BaseRepository<Parking, Long> {
}
