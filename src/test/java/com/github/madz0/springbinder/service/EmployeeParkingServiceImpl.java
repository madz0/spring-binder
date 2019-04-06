package com.github.madz0.springbinder.service;

import com.github.madz0.springbinder.binding.form.annotation.FormObject;
import com.github.madz0.springbinder.binding.rest.serialize.RestResultFactory;
import com.github.madz0.springbinder.model.Employee;
import com.github.madz0.springbinder.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeParkingServiceImpl implements EmployeeParkingService {
    @Autowired
    EmployeeRepository employeeRepository;

    @Override
    public Object create(@FormObject(group = Employee.EmployeeParkingEdit.class)
                                 Employee employee) {
        employee.getEmployeeParkings().forEach(ep->ep.setNameFromFile(ep.getFile().getOriginalFilename()));
        employeeRepository.save(employee);
        return RestResultFactory.created();
    }
}
