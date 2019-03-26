package com.github.madzo.springbinder.model;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.github.madz0.springbinder.model.IBaseModel;
import com.github.madz0.springbinder.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseModel implements IBaseModel<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Version
    protected Long version;

    @Override
    public BaseModel getCreatedBy() {
        return null;
    }

    @Override
    public BaseModel getModifiedBy() {
        return null;
    }

    @Override
    public String getPresentation() {
        return null;
    }

    @Override
    public Date getCreatedDate() {
        return null;
    }

    @Override
    public Time getCreatedTime() {
        return null;
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Time getModifiedTime() {
        return null;
    }
}
