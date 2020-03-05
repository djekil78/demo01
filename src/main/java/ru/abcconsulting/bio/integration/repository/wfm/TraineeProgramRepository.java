package ru.abcconsulting.bio.integration.repository.wfm;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.abcconsulting.bio.integration.entity.wfm.TraineeProgram;

public interface TraineeProgramRepository extends JpaRepository<TraineeProgram, Long> {
}
