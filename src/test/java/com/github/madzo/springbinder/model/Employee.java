package com.github.madzo.springbinder.model;

import java.util.List;
import java.util.Set;

import javax.persistence.*;

import ir.iiscenter.springform.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Employee extends BaseModel {

	@Column
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Company company;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private House house;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="employee")
	Set<Car> cars;

  @Override
  public String getPresentation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ValidationError> validate(Class<?> group) {
    // TODO Auto-generated method stub
    return null;
  }
}
