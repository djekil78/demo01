package ru.abcconsulting.bio.integration.dto;

import lombok.Data;

import java.util.List;

@Data
public class BioSmartPageLogDto {

    private Long count;
    private String next;
    private String previous;
    private List<BioSmartLogDto> results;

}
