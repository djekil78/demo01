package ru.abcconsulting.bio.integration.repository.wfm;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.abcconsulting.bio.integration.entity.wfm.RecordOrgUnit;

public interface RecordOrgUnitRepository extends JpaRepository<RecordOrgUnit, Long> {
    Long countByRecordId(Long recordId);
    void deleteByRecordId(Long recordId);
}
