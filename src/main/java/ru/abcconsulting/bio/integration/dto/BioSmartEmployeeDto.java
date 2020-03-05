package ru.abcconsulting.bio.integration.dto;

import lombok.Data;

@Data
public class BioSmartEmployeeDto {

    private Long id;
    private String first_name;
    private String last_name;
    private String middle_name;
    private String workernum;
    private String job;
    private String state;
    private Long parent;
    private String photo_preview;
    private boolean has_photo;

}
