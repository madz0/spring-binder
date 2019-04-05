package com.github.madz0.springbinder.model;

import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Company extends BaseModeId {

    public interface ICreate extends BaseGroups.ICreate {
        @Override
        default Set<IProperty> getProperties() {
            return createProps(Company.class,
                    model("employees",
                            model("house", field("id")),
                            field("name")),
                    model("city", field("id")),
                    field("name"));
        }
    }

    @Column
    @NotNull
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private City city;

    @Valid
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
