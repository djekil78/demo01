package ru.abcconsulting.bio.integration.dto;

import lombok.Data;

@Data
public class BioSmartLogDto {

    private Long id;
//    private Long outerId;
    private Long object_id;
    private Long subject_id;
    private Long event;
    private String time;
    private String object_tz;
//    private boolean integrated;

    public ExtendedBioSmartLogDto toExtendedBioSmartLogDto () {
        ExtendedBioSmartLogDto extendedBioSmartLogDto = new ExtendedBioSmartLogDto();
        extendedBioSmartLogDto.setOuterId(this.getId());
        extendedBioSmartLogDto.setObject_id(this.getObject_id());
        extendedBioSmartLogDto.setSubject_id(this.getSubject_id());
        extendedBioSmartLogDto.setEvent(this.getEvent());
        extendedBioSmartLogDto.setTime(this.getTime());
        extendedBioSmartLogDto.setObject_tz(this.getObject_tz());
        return extendedBioSmartLogDto;
    }
}
