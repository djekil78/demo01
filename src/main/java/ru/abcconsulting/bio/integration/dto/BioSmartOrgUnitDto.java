package ru.abcconsulting.bio.integration.dto;

import lombok.Data;

@Data
public class BioSmartOrgUnitDto {
    private Long id;
    private String name;
    private Long parent;
    private Long firm;
    private String depnum;
//    @JsonValue("is_firm")
    private Boolean is_firm;
}
