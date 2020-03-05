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
@Table(name = "person")
@Accessors(chain = true)
public class BioPerson extends DomainObject {

    @Column(name = "active")
    private boolean active;

    @Column(name = "external")
    private boolean external;

    @Column(name = "outerId")
    private String outerId;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "patronymicName")
    private String patronymicName;

    @Column(name = "lastName")
    private String lastName;
}
