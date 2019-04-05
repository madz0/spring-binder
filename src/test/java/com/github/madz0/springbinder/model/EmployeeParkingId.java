package com.github.madz0.springbinder.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode(of = {"employeeId", "parkingId"})
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"employeeId", "parkingId"})
@Embeddable
public class EmployeeParkingId implements Serializable {
    @Column(name = "employee_id")
    private Long employeeId;
    @Column(name = "parking_id")
    private Long parkingId;
}
