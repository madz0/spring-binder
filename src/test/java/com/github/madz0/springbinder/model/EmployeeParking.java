package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.validation.ValidationError;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
public class EmployeeParking extends BaseModel implements IBaseModelId<EmployeeParkingId> {
    @EmbeddedId
    private EmployeeParkingId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("employeeId")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("parkingId")
    private Parking parking;

    @Column
    private String nameFromFile;

    @Transient
    private MultipartFile file;

    @Override
    public List<ValidationError> validate(Class<?> group) {
        return null;
    }
}
