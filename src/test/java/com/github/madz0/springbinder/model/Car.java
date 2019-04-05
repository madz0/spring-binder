package com.github.madz0.springbinder.model;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.github.madz0.springbinder.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Car extends BaseModeId {

    @Column
    String model;
    @Column
    Date date;
    @Column
    Boolean hasSold;
    @Column
    @NotNull
    Time time;

    @ManyToOne(fetch = FetchType.EAGER)
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
