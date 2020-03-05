package ru.abcconsulting.bio.integration.entity.bio;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import ru.abcconsulting.bio.integration.entity.DomainObject;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "record")
public class BioRecord extends DomainObject {

    @Column(name = "alive")
    private boolean alive;

    @Column(name = "falseRecognition")
    private boolean falseRecognition;

    @Column(name = "integrated")
    private boolean integrated;

    @Column(name = "method")
    private Integer method;

    @Column(name = "purpose")
    private Integer purpose;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "faceCount")
    private Integer faceCount;

    @Column(name = "error")
    private Integer error;

    @Column(name = "zoneOffset")
    private Integer zoneOffset;

    @Column(name = "timestamp")
    private Long timestamp;

    @Column(name = "tolerance")
    private Double tolerance;

    @Column(name = "reply")
    private String reply;

    @Column(name = "personId")
    private String personId;

    @Column(name = "personName")
    private String personName;

    @Column(name = "terminalId")
    private String terminalId;

    @Column(name = "cid")
    private String cid;

    @Column(name = "ip")
    private String ip;

    @Column(name = "ipInfo")
    private String ipInfo;

    @Setter(AccessLevel.NONE)
    @CollectionTable(name = "record_persongroups", joinColumns = @JoinColumn(name = "record_id"))
    @Column(name = "PersonGroups")
    @ElementCollection(fetch=FetchType.EAGER)
    private Set<String> personGroups = new HashSet<>();

}
