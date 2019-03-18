package com.github.madzo.springbinder.service;

import com.github.madz0.springbinder.utils.rest.RestResult;
import com.github.madzo.springbinder.model.City;
import com.github.madzo.springbinder.repository.CityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
@Service
public class CityServiceImpl implements CityService {

	@Autowired
	CityRepository cityRepository;
	
	@Override
	public Object create(@RequestBody City city) {
		cityRepository.save(city);
		return new RestResult("OK", HttpStatus.OK, null);
	}

	@Override
	public Object list() {
		return new RestResult(cityRepository.findAll(), HttpStatus.OK, null);
	}
}
