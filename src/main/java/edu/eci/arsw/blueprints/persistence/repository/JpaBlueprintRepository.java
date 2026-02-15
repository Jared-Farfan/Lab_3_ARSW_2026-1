package edu.eci.arsw.blueprints.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.eci.arsw.blueprints.model.Blueprint;

@Repository
public interface JpaBlueprintRepository extends JpaRepository<Blueprint, Long> {

    Optional<Blueprint> findByAuthorAndName(String author, String name);

    List<Blueprint> findByAuthor(String author);

    boolean existsByAuthorAndName(String author, String name);
}
