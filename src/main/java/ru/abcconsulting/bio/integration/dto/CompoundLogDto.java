package ru.abcconsulting.bio.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompoundLogDto {
    private Long id;
    //    private Long bioSmartLogId;
    private Long hrportalEmployeeId;
    private Long hrportalOrgUnitId;
    private Long event;
    private String time;
    private String timezone;
}
