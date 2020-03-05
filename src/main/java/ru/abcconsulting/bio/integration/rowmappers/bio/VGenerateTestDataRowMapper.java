package ru.abcconsulting.bio.integration.rowmappers.bio;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.abcconsulting.bio.integration.dto.VGenerateTestDataDto;
import java.time.LocalDateTime;


import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class VGenerateTestDataRowMapper implements RowMapper {
    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        VGenerateTestDataDto vgeneratetestdata = new VGenerateTestDataDto();
        vgeneratetestdata.setShiftid(rs.getLong("shiftid"));
        vgeneratetestdata.setShiftemployeeid(rs.getLong("shiftemployeeid"));
        vgeneratetestdata.setPersonid(rs.getLong("personid"));
        vgeneratetestdata.setStartDateTime(rs.getObject("startDateTime",LocalDateTime.class));
        vgeneratetestdata.setEndDateTime(rs.getObject("endDateTime",LocalDateTime.class));
        //vgeneratetestdata.setStartDateTime(rs.getDate("startDateTime"));
        //vgeneratetestdata.setEndDateTime(rs.getDate("endDateTime"));

        return vgeneratetestdata;
    }
}
