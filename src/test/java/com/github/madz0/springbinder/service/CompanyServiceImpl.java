package com.github.madz0.springbinder.service;

import com.github.madz0.springbinder.binding.form.annotation.FormObject;
import com.github.madz0.springbinder.binding.rest.serialize.RestResultFactory;
import com.github.madz0.springbinder.model.Company;
import com.github.madz0.springbinder.model.dto.SomeDto;
import com.github.madz0.springbinder.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Object create(@Validated(Company.TestGroup.class)
                         @FormObject Company company, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return RestResultFactory.badRequest(bindingResult);
        }
        companyRepository.save(company);
        return RestResultFactory.created();
    }

    @Override
    public Object list() {
        return RestResultFactory.okay(companyRepository.findAll(), null);
    }

    public Object update(@Validated @FormObject(entityGraph = {"employees",
            "city", "employees.cars", "employees.cars.manufacture"}) Company company,
                         BindingResult bindingResult) {
        companyRepository.save(company);
        return RestResultFactory.created();
    }

    @Override
    public Object dto(@Validated @FormObject SomeDto someDto, BindingResult bindingResult) {
        return RestResultFactory.okay(someDto, null);
    }
}
