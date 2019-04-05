package com.github.madz0.springbinder.service;

import com.github.madz0.springbinder.binding.form.annotation.FormObject;
import com.github.madz0.springbinder.model.Company;
import com.github.madz0.springbinder.repository.CompanyRepository;
import com.github.madz0.springbinder.utils.rest.RestResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityManager;

@Service
public class CompanyServiceImpl implements CompanyService {

	@Autowired
	CompanyRepository companyRepository;
	@Autowired
	EntityManager entityManager;
	
	@Override
	public Object create(@Validated @FormObject Company company, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			return new RestResult(null, HttpStatus.BAD_REQUEST, null);
		}
		companyRepository.save(company);
		return new RestResult(null, HttpStatus.OK, null);
	}

	@Override
	public Object list() {
		return new RestResult(companyRepository.findAll(), HttpStatus.OK, null);
	}

	public Object update(@Validated @FormObject(entityGraph = {"employees",
			"city", "employees.cars", "employees.cars.manufacture"}) Company company,
						 BindingResult bindingResult) {
		companyRepository.save(company);
		return new RestResult(null, HttpStatus.OK, null);
	}
}
