package ru.abcconsulting.bio.integration.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@Data
@EqualsAndHashCode
public class VGenerateTestDataDto {

    private Long shiftid;
    private Long shiftemployeeid;
    private Long personid;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

}
