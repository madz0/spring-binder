package com.github.madz0.springbinder.service;

import com.github.madz0.springbinder.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employeeParking")
public interface EmployeeParkingService {
    @PostMapping("/create")
    Object create(Employee employee);
}
