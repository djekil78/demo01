package ru.abcconsulting.bio.integration.entity.wfm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

import ru.abcconsulting.bio.integration.entity.DomainObject;

@Getter
@Setter
@Entity
@Table(name = "record")
@Accessors(chain = true)
@NoArgsConstructor
public class WfmRecord extends DomainObject {

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "purpose")
    private Integer purpose;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "zoneOffset")
    private String zoneOffset;

    public WfmRecord(Long id) {
        setId(id);
    }
}
