package com.github.madz0.springbinder.model;

import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.validation.Valid;

import com.github.madz0.springbinder.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Employee extends BaseModeId {

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    private House house;

    @Valid
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "employee")
    Set<Car> cars;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "employee", orphanRemoval = true, fetch = FetchType.LAZY)
    Set<EmployeeParking> employeeParkings;

    @Override
    public String getPresentation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ValidationError> validate(Class<?> group) {
        // TODO Auto-generated method stub
        return null;
    }
}
