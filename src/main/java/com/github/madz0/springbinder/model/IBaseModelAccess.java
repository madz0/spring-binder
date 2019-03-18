package com.github.madz0.springbinder.model;

public interface IBaseModelAccess<ID> {
    String ID_FIELD = "id";
    ID getId();
    void setId(ID id);

    String ACCESS_FIELD = "access";
    default String[] getAccess(){
        return null;
    }

}
