package ru.abcconsulting.bio.integration.repository.wfm;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.abcconsulting.bio.integration.entity.wfm.IdTraineeEmployee;
import ru.abcconsulting.bio.integration.entity.wfm.TraineeProgramEmployee;

public interface TraineeProgramEmployeeRepository extends JpaRepository<TraineeProgramEmployee, IdTraineeEmployee> {
}
