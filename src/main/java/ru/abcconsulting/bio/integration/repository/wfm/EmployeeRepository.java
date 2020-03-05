package ru.abcconsulting.bio.integration.repository.wfm;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.abcconsulting.bio.integration.entity.wfm.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
