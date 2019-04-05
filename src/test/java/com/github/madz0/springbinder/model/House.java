package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.List;

@Getter
@Setter
@Entity
public class House extends BaseModeId{

	@Column
	private String address;
	
	@ManyToOne
	private City city;

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
