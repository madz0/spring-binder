package com.github.madz0.springbinder.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.github.madz0.springbinder.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CarManufacture extends IdBaseModel {

    @Column
    String name;

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
