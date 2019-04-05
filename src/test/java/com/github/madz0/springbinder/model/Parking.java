package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Parking extends BaseModeId {
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parking", orphanRemoval = true, fetch = FetchType.LAZY)
    Set<EmployeeParking> employeeParkings;

    @Override
    public List<ValidationError> validate(Class<?> group) {
        return null;
    }
}
