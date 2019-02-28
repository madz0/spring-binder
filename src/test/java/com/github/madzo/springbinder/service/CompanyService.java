package com.github.madzo.springbinder.service;

import com.github.madz0.springbinder.annotation.FormObject;
import com.github.madzo.springbinder.model.Company;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/company")
public interface CompanyService {

	@RequestMapping("/create")
	Object create(@FormObject Company company);
	@RequestMapping("/list")
	Object list();
	@RequestMapping("/update")
	Object update(@Valid @FormObject Company company);
}
