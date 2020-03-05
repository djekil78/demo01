package ru.abcconsulting.bio.integration.repository.integration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ru.abcconsulting.bio.integration.entity.integration.ExtendedBioSmartLog;

public interface ExtendedBioSmartLogRepository extends JpaRepository<ExtendedBioSmartLog, Long>, QuerydslPredicateExecutor<ExtendedBioSmartLog> {

    @Query("SELECT coalesce(max(l.outerId), 0) FROM ExtendedBioSmartLog l")
    Long getMaxOuterId();
}
