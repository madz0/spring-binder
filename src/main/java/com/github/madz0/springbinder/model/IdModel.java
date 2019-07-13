package com.github.madz0.springbinder.model;

public interface IdModel<ID> {
    String ID_FIELD = "id";
    ID getId();
    void setId(ID id);
}
