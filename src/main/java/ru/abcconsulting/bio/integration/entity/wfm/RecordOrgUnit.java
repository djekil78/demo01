package ru.abcconsulting.bio.integration.entity.wfm;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import ru.abcconsulting.bio.integration.entity.DomainObject;

@Getter
@Setter
@Entity
@Table(name = "record_organizationunit")
@Accessors(chain = true)
public class RecordOrgUnit extends DomainObject {

    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "organizationUnit_id")
    private Long orgUnitId;
}
