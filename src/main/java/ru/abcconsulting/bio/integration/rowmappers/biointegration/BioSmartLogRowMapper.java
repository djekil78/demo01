package ru.abcconsulting.bio.integration.rowmappers.biointegration;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.abcconsulting.bio.integration.dto.ExtendedBioSmartLogDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class BioSmartLogRowMapper implements RowMapper {
    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        ExtendedBioSmartLogDto extendedBioSmartLogDto = new ExtendedBioSmartLogDto();
        extendedBioSmartLogDto.setId(rs.getLong("id"));
        extendedBioSmartLogDto.setOuterId(rs.getLong("outerId"));
        extendedBioSmartLogDto.setObject_id(rs.getLong("object_id"));
        extendedBioSmartLogDto.setSubject_id(rs.getLong("subject_id"));
        extendedBioSmartLogDto.setEvent(rs.getLong("event"));
        extendedBioSmartLogDto.setTime(rs.getString("time"));
        extendedBioSmartLogDto.setObject_tz(rs.getString("object_tz"));
        extendedBioSmartLogDto.setIntegrated(rs.getBoolean("integrated"));
        return extendedBioSmartLogDto;
    }
