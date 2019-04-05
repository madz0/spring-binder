package com.github.madz0.springbinder.service;

import com.github.madz0.springbinder.model.City;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/city")
public interface CityService {

	@RequestMapping(path="/create", method=RequestMethod.POST)
	Object create(@RequestBody City city);
	@RequestMapping("/list")
	Object list();
}
