package ru.abcconsulting.bio.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedBioSmartLogDto {

    private Long id;
    private Long outerId;
    private Long object_id;
    private Long subject_id;
    private Long event;
    private String time;
    private String object_tz;
    private boolean integrated;

}
