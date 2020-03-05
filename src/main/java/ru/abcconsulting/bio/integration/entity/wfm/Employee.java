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
@Table(name = "employee")
public class Employee extends DomainObject {

    @Column(name = "active")
    private Boolean active;

    @Column(name = "dummy")
    private Boolean dummy;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "startWorkDate")
    private Date startWorkDate;

    @Column(name = "endWorkDate")
    private Date endWorkDate;

    @Column(name = "traineeProgram_id")
    private Long traineeProgramId;

    @Column(name = "email")
    private String email;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "inn")
    private String inn;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "outerId")
    private String outerId;

    @Column(name = "patronymicName")
    private String patronymicName;

    @Column(name = "snils")
    private String snils;

    @Column(name = "avatarPath")
    private String avatarPath;
}
