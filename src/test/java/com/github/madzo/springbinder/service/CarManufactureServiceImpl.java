package com.github.madzo.springbinder.service;

import com.github.madzo.springbinder.model.CarManufacture;
import com.github.madzo.springbinder.repository.CarManufactureRepository;
import ir.iiscenter.springform.utils.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class CarManufactureServiceImpl implements CarManufactureService {

	@Autowired
	CarManufactureRepository carManufactureRepository;
	
	@Override
	public Object create(@RequestBody CarManufacture carManufacture) {
		carManufactureRepository.save(carManufacture);
		return new RestResult("OK", HttpStatus.OK, null);
	}

	@Override
	public Object list() {
		return new RestResult(carManufactureRepository.findAll(), HttpStatus.OK, null);
	}

}
