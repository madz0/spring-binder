package com.github.madz0.springbinder.service;

import com.github.madz0.springbinder.model.CarManufacture;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carmanufacture")
public interface CarManufactureService {

	@RequestMapping("/create")
	Object create(@RequestBody CarManufacture carManufacture);
	
	@RequestMapping("/list")
	Object list();
}
