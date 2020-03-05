package ru.abcconsulting.bio.integration.entity.wfm;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Date;

import ru.abcconsulting.bio.integration.entity.DomainObject;

@Getter
@Setter
@Entity
@Table(name = "employeeposition")
public class EmployeePosition extends DomainObject {

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "startDate")
    private Date startDate;

    @Column(name = "endDate")
    private Date endDate;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() == this.getClass()) {
            EmployeePosition ePos = (EmployeePosition) obj;
            if (ePos.employeeId.equals(this.employeeId) && ePos.positionId.equals(this.positionId) && ePos.startDate == this.startDate) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + employeeId.hashCode();
        result = 31 * result + positionId.hashCode();
        result = 31 * result + startDate.hashCode();
        return result;
    }
}
