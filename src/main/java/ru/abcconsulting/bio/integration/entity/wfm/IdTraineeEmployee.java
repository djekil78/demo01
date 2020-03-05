package ru.abcconsulting.bio.integration.entity.wfm;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class IdTraineeEmployee implements Serializable {

    private Long traineeProgramId;

    private Long mentorsId;
}
