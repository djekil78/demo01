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
@Table(name = "position")
public class Position extends DomainObject {

    @Column(name = "startDate")
    private Date startDate;

    @Column(name = "endDate")
    private Date endDate;

    @Column(name = "name")
    private String name;

    @Column(name = "outerId")
    private String outerId;

    @Column(name = "organizationUnit_id")
    private Long orgUnitId;
}
