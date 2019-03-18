package com.github.madz0.springbinder.model;

import java.util.Date;

public interface IBaseModel<ID> extends IBaseModelAccess<ID>, IValidate {
    String VERSION_FIELD = "version";
    Long getVersion();
    void setVersion(Long version);

    String CREATED_DATE_FIELD = "createdDate";
    Date getCreatedDate();

    String MODIFIED_DATE_FIELD = "modifiedDate";
    Date getModifiedDate();

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
