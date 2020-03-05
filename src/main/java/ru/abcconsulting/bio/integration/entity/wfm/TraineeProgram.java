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
@Table(name = "traineeprogram")
public class TraineeProgram extends DomainObject {

    @Column(name = "endDate")
    private Date endDate;
}
