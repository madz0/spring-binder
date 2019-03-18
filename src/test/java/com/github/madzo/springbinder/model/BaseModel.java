package com.github.madzo.springbinder.model;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.github.madz0.springbinder.model.IBaseModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseModel implements IBaseModel<Long> {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Long id;
	
	@Version
	protected Long version;

	@Override
	public BaseModel getCreatedBy() {
		return null;
	}

	@Override
	public BaseModel getModifiedBy() {
		return null;
	}

	@Override
	public Date getCreatedDate() {
		return null;
	}

	@Override
	public Date getModifiedDate() {
		return null;
	}
}
