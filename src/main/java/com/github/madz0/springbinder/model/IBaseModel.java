package com.github.madz0.springbinder.model;

import java.time.LocalDate;
import java.time.LocalTime;

public interface IBaseModel<ID> extends IBaseModelAccess, IValidate {
    String VERSION_FIELD = "version";
    Long getVersion();
    void setVersion(Long version);

    String CREATED_DATE_FIELD = "createdDate";
    LocalDate getCreatedDate();

    String CREATED_TIME_FIELD = "createdTime";
    LocalTime getCreatedTime();

    String MODIFIED_DATE_FIELD = "modifiedDate";
    LocalDate getModifiedDate();

    String MODIFIED_TIME_FIELD = "modifiedTime";
    LocalTime getModifiedTime();

    String CREATED_BY_FIELD = "createdBy";
    IBaseModel<ID> getCreatedBy();

    String MODIFIED_BY_FIELD = "modifiedBy";
    IBaseModel<ID> getModifiedBy();

    String PRESENTATION_FIELD = "presentation";
    String getPresentation();

    String RECORD_TYPE_FIELD = "recordType";
    default String getRecordType(){
        return getClass().getName();
    }
}
