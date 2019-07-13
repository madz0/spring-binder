package com.github.madz0.springbinder.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@EntityListeners(BaseModel.AuditListener.class)
@MappedSuperclass
public abstract class BaseModel implements IModel<Long>, Serializable {

    @Version
    protected Long version;

    @Column
    private LocalDate createdDate;
    @Column
    private LocalDate modifiedDate;
    @Column
    private LocalTime createdTime;
    @Column
    private LocalTime modifiedTime;

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

    public static class AuditListener {
        @PrePersist
        void onPrePersist(Object o) {
            if (o instanceof BaseModel) {
                LocalDate d = LocalDate.now();
                BaseModel obj = (BaseModel) o;
                if (obj.getCreatedDate() == null) {
                    obj.setCreatedDate(d);
                    obj.setModifiedDate(d);
                }
                if (obj.getCreatedTime() == null) {
                    LocalTime t = LocalTime.now();
                    obj.setCreatedTime(t);
                    obj.setModifiedTime(t);
                }
            }
        }

        @PreUpdate
        void onPreUpdate(Object o) {
            if (o instanceof BaseModel) {
                BaseModel obj = (BaseModel) o;
                if (obj.getModifiedDate() == null) {
                    obj.setModifiedDate(LocalDate.now());
                }
                if (obj.getModifiedTime() == null) {
                    obj.setModifiedTime(LocalTime.now());
                }
            }
        }
    }
}
