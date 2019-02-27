package com.github.madzo.springbinder.model;

import java.sql.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import ir.iiscenter.springform.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Car extends BaseModel {

    @Column
    String model;
    @Column
    Date date;
    @Column
    Boolean hasSold;

    @ManyToOne
    CarManufacture manufacture;

    @ManyToOne
    Employee employee;

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
