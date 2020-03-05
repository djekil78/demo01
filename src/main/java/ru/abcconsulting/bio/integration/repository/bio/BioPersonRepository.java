package ru.abcconsulting.bio.integration.repository.bio;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.abcconsulting.bio.integration.entity.bio.BioPerson;

public interface BioPersonRepository extends JpaRepository<BioPerson, Long> {
    BioPerson findOneById(Long id);
}
