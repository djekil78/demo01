package ru.abcconsulting.bio.integration.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

import ru.abcconsulting.bio.integration.dto.VGenerateTestDataDto;
import ru.abcconsulting.bio.integration.rowmappers.bio.VGenerateTestDataRowMapper;

@Service
@Transactional
public class GenerateTestDataService {

    private List<VGenerateTestDataDto> vGenerateTestData;

    private BufferedWriter bwr;

    @Autowired
    @Qualifier("bioJdbcTemplate")
    private JdbcTemplate bioJdbcTemplate;

    @Autowired
    private VGenerateTestDataRowMapper vGenerateTestDataRowMapper;


    public void generateTestData() {

        getVGenerateTestData();
        generateInsert();
    }

    private void generateInsert() {
        StringBuffer insertStart = new StringBuffer();
        StringBuffer insertEnd = new StringBuffer();

        try {
            bwr = new BufferedWriter(new FileWriter(new File("c://tmp//insert.txt")));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }


        vGenerateTestData.forEach(v -> {

            int index = vGenerateTestData.indexOf(v);

            insertStart.setLength(0);
            insertEnd.setLength(0);


            long timestampStart = Timestamp.valueOf(v.getStartDateTime()).getTime();// + (long) getRandomNumberInRange(0, 600000);
            long timestampEnd = Timestamp.valueOf(v.getEndDateTime()).getTime();// - (long) getRandomNumberInRange(0, 600000);


            // для каждой пятой строки вставляем опоздание
            if ((index % 5) == 0) {
                timestampStart += 10800000L; // опоздание на 3 часа
                timestampEnd -= getRandomNumberInRange(0, 600000); // рандомный уход раньше на 5 минут

            } else if ((index % 7) == 0) {
                // для каждой 7 строки вставляем null end
                timestampEnd = 0L;
            } else if ((index % 9) == 0) {
                // для каждой 9 строки вставляем null start
                timestampStart = 0L;
            } else {
                timestampStart += getRandomNumberInRange(0, 600000); // рандомный приход позже на 5 минут
                timestampEnd -= getRandomNumberInRange(0, 600000); // рандомный уход раньше на 5 минут

            }

            insertStart.append("INSERT INTO record (timestamp,personId, purpose, tolerance,rating,falseRecognition,faceCount,alive,personName) VALUES(");
            insertEnd.append("INSERT INTO record (timestamp,personId, purpose, tolerance,rating,falseRecognition,faceCount,alive,personName) VALUES(");
            insertStart.append(timestampStart == 0L ? "null" : timestampStart).append(",'").append(v.getPersonid()).append("',0,0,0,true,1,true,'FIO');\n");
            insertEnd.append(timestampEnd == 0L ? "null" : timestampEnd).append(",'").append(v.getPersonid()).append("',1,0,0,true,1,true,'FIO');\n");

            try {
                //write contents of StringBuffer to a file
                bwr.write(insertStart.toString());
                bwr.write(insertEnd.toString());

            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            //System.out.println("Content of StringBuffer written to File.");
        });
        try {
            //flush the stream
            bwr.flush();
            //close the stream
            bwr.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    // random
    private int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    // helpers
    @Transactional(readOnly = true)
    public List<VGenerateTestDataDto> getVGenerateTestData() {

        String selectSql = "SELECT * FROM v_generate_testdata limit 5000";
        vGenerateTestData = bioJdbcTemplate.query(selectSql, vGenerateTestDataRowMapper);
        return vGenerateTestData;
    }


}
