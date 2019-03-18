package com.github.madzo.springbinder.test;

import com.github.madzo.springbinder.model.Company;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DtoContainsModel {
    String name;
    private Company company;
}
