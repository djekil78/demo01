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
@Table(name = "organizationunit")
public class OrgUnit extends DomainObject {

    @Column(name = "active")
    private Boolean active;

    @Column(name = "chiefPosition_id")
    private Long chiefPositionId;

    @Column(name = "organizationUnitType_id")
    private Long orgUnitTypeId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "dateFrom")
    private Date dateFrom;

    @Column(name = "dateTo")
    private Date dateTo;

    @Column(name = "email")
    private String email;

    @Column(name = "fax")
    private String fax;

    @Column(name = "name")
    private String name;

    @Column(name = "outerId")
    private String outerId;
}
