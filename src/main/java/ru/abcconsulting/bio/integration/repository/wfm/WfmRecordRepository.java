package ru.abcconsulting.bio.integration.repository.wfm;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.abcconsulting.bio.integration.entity.wfm.WfmRecord;

public interface WfmRecordRepository extends JpaRepository<WfmRecord, Long> {
}
