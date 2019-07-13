package com.github.madz0.springbinder.test;

import com.github.madz0.springbinder.binding.IdClassMapper;
import com.github.madz0.springbinder.model.EmployeeParking;
import com.github.madz0.springbinder.model.EmployeeParkingId;
import com.github.madz0.springbinder.model.IdModel;
import org.springframework.stereotype.Component;

@Component
public class IdClassMapperImpl implements IdClassMapper {
    @Override
    public Class<?> getIdClassOf(Class<?> entity) {
        if (entity == EmployeeParking.class) {
            return EmployeeParkingId.class;
        } else if (IdModel.class.isAssignableFrom(entity)) {
            return Long.class;
        }
        return null;
    }
}
