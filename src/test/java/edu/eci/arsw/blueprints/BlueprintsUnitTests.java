package edu.eci.arsw.blueprints;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.eci.arsw.blueprints.filters.IdentityFilter;
import edu.eci.arsw.blueprints.filters.RedundancyFilter;
import edu.eci.arsw.blueprints.filters.UndersamplingFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.persistence.InMemoryBlueprintPersistence;
import edu.eci.arsw.blueprints.services.BlueprintsServices;

class BlueprintsUnitTests {

    // ========== Tests del Modelo Blueprint ==========
    
    @Test
    void testBlueprintCreation() {
        Blueprint bp = new Blueprint("autor1", "plano1");
        assertEquals("autor1", bp.getAuthor());
        assertEquals("plano1", bp.getName());
        assertTrue(bp.getPoints().isEmpty());
    }

    @Test
    void testBlueprintWithPoints() {
        List<Point> points = List.of(new Point(0, 0), new Point(10, 10));
        Blueprint bp = new Blueprint("autor1", "plano1", points);
        assertEquals(2, bp.getPoints().size());
        assertEquals(0, bp.getPoints().get(0).getX());
        assertEquals(10, bp.getPoints().get(1).getY());
    }

    @Test
    void testAddPointToBlueprint() {
        Blueprint bp = new Blueprint("autor1", "plano1");
        bp.addPoint(new Point(5, 5));
        assertEquals(1, bp.getPoints().size());
    }

    // ========== Tests del Filtro de Redundancia ==========

    @Test
    void testRedundancyFilterRemovesDuplicates() {
        RedundancyFilter filter = new RedundancyFilter();
        // Puntos con duplicados consecutivos
        List<Point> points = List.of(
            new Point(0, 0), 
            new Point(0, 0),  // duplicado
            new Point(10, 10), 
            new Point(10, 10), // duplicado
            new Point(20, 20)
        );
        Blueprint bp = new Blueprint("autor", "plano", points);
        Blueprint filtered = filter.apply(bp);
        
        assertEquals(3, filtered.getPoints().size()); // Solo 3 puntos únicos consecutivos
    }

    @Test
    void testRedundancyFilterEmptyBlueprint() {
        RedundancyFilter filter = new RedundancyFilter();
        Blueprint bp = new Blueprint("autor", "plano");
        Blueprint filtered = filter.apply(bp);
        assertTrue(filtered.getPoints().isEmpty());
    }

    // ========== Tests del Filtro de Undersampling ==========

    @Test
    void testUndersamplingFilterKeepsEvenIndexes() {
        UndersamplingFilter filter = new UndersamplingFilter();
        List<Point> points = List.of(
            new Point(0, 0),  // índice 0 - se mantiene
            new Point(1, 1),  // índice 1 - se elimina
            new Point(2, 2),  // índice 2 - se mantiene
            new Point(3, 3),  // índice 3 - se elimina
            new Point(4, 4)   // índice 4 - se mantiene
        );
        Blueprint bp = new Blueprint("autor", "plano", points);
        Blueprint filtered = filter.apply(bp);
        
        assertEquals(3, filtered.getPoints().size());
        assertEquals(0, filtered.getPoints().get(0).getX());
        assertEquals(2, filtered.getPoints().get(1).getX());
        assertEquals(4, filtered.getPoints().get(2).getX());
    }

    @Test
    void testUndersamplingFilterSmallBlueprint() {
        UndersamplingFilter filter = new UndersamplingFilter();
        List<Point> points = List.of(new Point(0, 0), new Point(1, 1));
        Blueprint bp = new Blueprint("autor", "plano", points);
        Blueprint filtered = filter.apply(bp);
        
        // Con 2 o menos puntos, no se filtra
        assertEquals(2, filtered.getPoints().size());
    }

    // ========== Tests de Persistencia en Memoria ==========

    @Test
    void testInMemoryPersistenceGetBlueprint() throws BlueprintNotFoundException {
        InMemoryBlueprintPersistence persistence = new InMemoryBlueprintPersistence();
        // Los datos de ejemplo incluyen "john" con "house"
        Blueprint bp = persistence.getBlueprint("john", "house");
        assertNotNull(bp);
        assertEquals("john", bp.getAuthor());
        assertEquals("house", bp.getName());
    }

    @Test
    void testInMemoryPersistenceSaveBlueprint() throws BlueprintPersistenceException, BlueprintNotFoundException {
        InMemoryBlueprintPersistence persistence = new InMemoryBlueprintPersistence();
        Blueprint newBp = new Blueprint("nuevoAutor", "nuevoPlano", 
            List.of(new Point(0, 0), new Point(5, 5)));
        persistence.saveBlueprint(newBp);
        
        Blueprint retrieved = persistence.getBlueprint("nuevoAutor", "nuevoPlano");
        assertEquals("nuevoAutor", retrieved.getAuthor());
    }

    @Test
    void testInMemoryPersistenceDuplicateFails() {
        InMemoryBlueprintPersistence persistence = new InMemoryBlueprintPersistence();
        Blueprint duplicate = new Blueprint("john", "house"); // Ya existe
        
        assertThrows(BlueprintPersistenceException.class, () -> {
            persistence.saveBlueprint(duplicate);
        });
    }

    @Test
    void testInMemoryPersistenceNotFound() {
        InMemoryBlueprintPersistence persistence = new InMemoryBlueprintPersistence();
        
        assertThrows(BlueprintNotFoundException.class, () -> {
            persistence.getBlueprint("noExiste", "nada");
        });
    }

    @Test
    void testInMemoryPersistenceGetByAuthor() throws BlueprintNotFoundException {
        InMemoryBlueprintPersistence persistence = new InMemoryBlueprintPersistence();
        Set<Blueprint> johnBlueprints = persistence.getBlueprintsByAuthor("john");
        assertEquals(2, johnBlueprints.size()); // john tiene "house" y "garage"
    }

    // ========== Tests del Servicio ==========

    @Test
    void testServiceGetAllBlueprints() {
        InMemoryBlueprintPersistence persistence = new InMemoryBlueprintPersistence();
        IdentityFilter filter = new IdentityFilter();
        BlueprintsServices service = new BlueprintsServices(persistence, filter);
        
        Set<Blueprint> all = service.getAllBlueprints();
        assertEquals(3, all.size()); // john/house, john/garage, jane/garden
    }

    @Test
    void testServiceAddBlueprint() throws BlueprintPersistenceException {
        InMemoryBlueprintPersistence persistence = new InMemoryBlueprintPersistence();
        IdentityFilter filter = new IdentityFilter();
        BlueprintsServices service = new BlueprintsServices(persistence, filter);
        
        Blueprint newBp = new Blueprint("testAuthor", "testBlueprint");
        service.addNewBlueprint(newBp);
        
        Set<Blueprint> all = service.getAllBlueprints();
        assertEquals(4, all.size());
    }
}
