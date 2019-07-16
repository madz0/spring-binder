package com.github.madz0.springbinder.service;

import com.github.madz0.springbinder.model.Company;
import com.github.madz0.springbinder.model.dto.SomeDto;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
public interface CompanyService {

	@RequestMapping("/create")
	Object create(Company company, BindingResult bindingResult);
	@RequestMapping("/list")
	Object list();
	@RequestMapping("/update")
	Object update(Company company, BindingResult bindingResult);
	@RequestMapping("/dto")
	Object dto(SomeDto someDto, BindingResult bindingResult);
	@RequestMapping("/anotherDto")
	Object anotherDto(SomeDto someDto, BindingResult bindingResult);
}
