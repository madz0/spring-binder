package com.github.madzo.springbinder.service;

import com.github.madz0.springbinder.annotation.FormObject;
import com.github.madzo.springbinder.model.Company;
import com.github.madzo.springbinder.repository.CompanyRepository;
import ir.iiscenter.springform.utils.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.persistence.EntityManager;
import javax.validation.Valid;

@Service
public class CompanyServiceImpl implements CompanyService {

	@Autowired
	CompanyRepository companyRepository;
	@Autowired
	EntityManager entityManager;
	
	@Override
	public Object create(@Valid @FormObject Company company) {
//		if(binding.hasErrors()) {
//			return new RestResult(null, HttpStatus.BAD_REQUEST, null);
//		}
		companyRepository.save(company);
		return new RestResult(null, HttpStatus.OK, null);
	}

	@Override
	public Object list() {
		return new RestResult(companyRepository.findAll(), HttpStatus.OK, null);
	}

	public Object update(@Valid @FormObject(entityGraph = {"employees",
			"city", "employees.cars", "employees.cars.manufacture"}) Company company) {
		companyRepository.save(company);
		return new RestResult(null, HttpStatus.OK, null);
	}
}
