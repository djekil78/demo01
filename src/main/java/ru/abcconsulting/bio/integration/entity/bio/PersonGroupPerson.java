package ru.abcconsulting.bio.integration.entity.bio;

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
@Accessors(chain = true)
@Table(name = "persongroupperson")
public class PersonGroupPerson extends DomainObject {

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "personGroup_id")
    private Long personGroupId;

    @Column(name = "positionName")
    private String positionName;
}
