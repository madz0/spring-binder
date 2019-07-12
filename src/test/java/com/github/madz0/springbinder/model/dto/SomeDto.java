package com.github.madz0.springbinder.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SomeDto {
    public String name;
    public String family;
    public Map<String, String> someMap;
}
