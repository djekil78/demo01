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
@Table(name = "persongroup")
public class BioPersonGroup extends DomainObject {

    @Column(name = "external")
    private Boolean external;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "outerId")
    private String outerId;

    @Column(name = "name")
    private String name;
}
