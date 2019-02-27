package com.github.madzo.springbinder.model;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import ir.iiscenter.springform.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Employee extends BaseModel {

	@Column
	private String name;
	
	@ManyToOne
	private Company company;
	
	@ManyToOne
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
