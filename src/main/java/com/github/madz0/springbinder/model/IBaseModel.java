package com.github.madz0.springbinder.model;

import java.sql.Time;
import java.util.Date;

public interface IBaseModel<ID> extends IBaseModelAccess, IValidate {
    String VERSION_FIELD = "version";
    Long getVersion();
    void setVersion(Long version);

    String CREATED_DATE_FIELD = "createdDate";
    Date getCreatedDate();

    String CREATED_TIME_FIELD = "createdTime";
    Time getCreatedTime();

    String MODIFIED_DATE_FIELD = "modifiedDate";
    Date getModifiedDate();

    String MODIFIED_TIME_FIELD = "modifiedTime";
    Time getModifiedTime();

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
