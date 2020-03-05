package ru.abcconsulting.bio.integration.repository.bio;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.abcconsulting.bio.integration.entity.bio.BioRecord;

import java.util.List;

public interface BioRecordRepository extends JpaRepository<BioRecord, Long> {
    List<BioRecord> findBioRecordByErrorIsNullAndAliveIsTrueAndPersonIdIsNotNullAndIntegratedIsFalse();
}
