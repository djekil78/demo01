package ru.abcconsulting.bio.integration.dto;

import lombok.Data;

@Data
public class ComposedEmployeePositionDto {
    private Long employeeId;
    private Long positionId;
    private String positionName;
    private Long orgUnitId;

}
