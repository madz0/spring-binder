package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.processor.annotation.FieldExtractor;
import java.time.LocalDate;
import java.time.LocalTime;

@FieldExtractor(as = "ModelFields")
public interface Model<ID> extends AccessModel, Validate {

    Long getVersion();
    void setVersion(Long version);

    LocalDate getCreatedDate();

    LocalTime getCreatedTime();

    LocalDate getModifiedDate();

    LocalTime getModifiedTime();

    Model<ID> getCreatedBy();

    Model<ID> getModifiedBy();

    String getPresentation();

    default String getRecordType(){
        return getClass().getName();
    }
}
