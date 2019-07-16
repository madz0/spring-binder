package com.github.madz0.springbinder.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SomeDto {
    public enum X{
        X1,
        X2;
    }
    public String name;
    public String family;
    public Map<String, String> someMap;
    public List<Map<String, X>> mapList;

    public String name_with_underscore;
}
