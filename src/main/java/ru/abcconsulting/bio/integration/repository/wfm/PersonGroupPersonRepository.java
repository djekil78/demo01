package ru.abcconsulting.bio.integration.repository.bio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ru.abcconsulting.bio.integration.entity.bio.PersonGroupPerson;

public interface PersonGroupPersonRepository extends JpaRepository<PersonGroupPerson, Long>, QuerydslPredicateExecutor<PersonGroupPerson> {
}
