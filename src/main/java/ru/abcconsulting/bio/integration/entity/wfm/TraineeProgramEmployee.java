package ru.abcconsulting.bio.integration.entity.wfm;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@IdClass(IdTraineeEmployee.class)
@Table(name = "traineeprogram_employee")
public class TraineeProgramEmployee {

    @Id
    @Column(name = "TraineeProgram_id")
    private Long traineeProgramId;

    @Id
    @Column(name = "mentors_id")
    private Long mentorsId;
}
