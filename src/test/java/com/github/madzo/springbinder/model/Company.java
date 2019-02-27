package com.github.madzo.springbinder.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import ir.iiscenter.springform.bind.property.IProperty;
import ir.iiscenter.springform.model.BaseGroups;
import ir.iiscenter.springform.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Company extends BaseModel {

    public interface ICreate extends BaseGroups.ICreate {
        @Override
        default Set<IProperty> getProperties() {
            return createProps(Company.class,
                    model("employees",
                            model("house", field("id")),
                            field("name")),
                    model("city", field("id")));
        }
    }

    @Column
    private String name;

    @ManyToOne(optional = false)
    private City city;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "company")
    private Set<Employee> employees;

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
