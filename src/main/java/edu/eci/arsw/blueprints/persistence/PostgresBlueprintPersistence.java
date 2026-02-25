package edu.eci.arsw.blueprints.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.repository.JpaBlueprintRepository;


public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final JpaBlueprintRepository repository;

    public PostgresBlueprintPersistence(JpaBlueprintRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (repository.existsByAuthorAndName(bp.getAuthor(), bp.getName())) {
            throw new BlueprintPersistenceException(
                "Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }
        repository.save(bp);
    }

    @Override
    @Transactional(readOnly = true)
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        return repository.findByAuthorAndName(author, name)
            .orElseThrow(() -> new BlueprintNotFoundException(
                "Blueprint not found: %s/%s".formatted(author, name)));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<Blueprint> blueprints = repository.findByAuthor(author);
        if (blueprints.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        return new HashSet<>(blueprints);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Blueprint> getAllBlueprints() {
        return new HashSet<>(repository.findAll());
    }

    @Override
    @Transactional
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        Blueprint bp = repository.findByAuthorAndName(author, name)
            .orElseThrow(() -> new BlueprintNotFoundException(
                "Blueprint not found: %s/%s".formatted(author, name)));
        
        bp.addPoint(new Point(x, y));
        repository.save(bp);
    }

    @Override
    @Transactional
    public void deleteBlueprint(String author, String name) throws BlueprintNotFoundException {
        if (!repository.existsByAuthorAndName(author, name)) {
            throw new BlueprintNotFoundException(
                "Blueprint not found: %s/%s".formatted(author, name));
        }
        repository.deleteByAuthorAndName(author, name);
    }
}
