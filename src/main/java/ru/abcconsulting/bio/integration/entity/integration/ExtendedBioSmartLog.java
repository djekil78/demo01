package ru.abcconsulting.bio.integration.entity.integration;

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
@Table(name = "record")
@Accessors(chain = true)
public class ExtendedBioSmartLog extends DomainObject {

    @Column(name = "integrated", nullable = false)
    private boolean integrated;

    @Column(name = "outerId", nullable = false)
    private Long outerId;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "event", nullable = false)
    private Long event;

    @Column(name = "time", nullable = false)
    private String time;

    @Column(name = "object_tz", nullable = false)
    private String objectTz;
}
