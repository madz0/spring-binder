package com.github.madz0.springbinder.test;

import com.github.madz0.springbinder.model.Company;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DtoContainsModel {
    String name;
    private Company company;
}
