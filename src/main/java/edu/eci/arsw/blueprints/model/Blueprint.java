package edu.eci.arsw.blueprints.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "blueprints", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"author", "name"})
})
public class Blueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "blueprint_points", joinColumns = @JoinColumn(name = "blueprint_id"))
    @OrderColumn(name = "point_order")
    private List<Point> points = new ArrayList<>();

    public Blueprint() {}
    public Blueprint(String author, String name) {
        this.author = author;
        this.name = name;
    }

    /* Constructor for Blueprint
    */
    public Blueprint(String author, String name, List<Point> pts) {
        this.author = author;
        this.name = name;
        if (pts != null) points.addAll(pts);
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    /* Returns an unmodifiable view of the points list
     */
    public List<Point> getPoints() { return Collections.unmodifiableList(points); }
    /* Adds a point to the blueprint
     */
    public void addPoint(Point p) { points.add(p); }
    /* Overrides equals and hashCode based on author and name
     */
    public void addPoints(List<Point> pts) {
        if (pts != null) points.addAll(pts);
    }
    public void setPoints(List<Point> pts) {
        points.clear();
        if (pts != null) points.addAll(pts);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blueprint bp)) return false;
        return Objects.equals(author, bp.author) && Objects.equals(name, bp.name);
    }
    /* Overrides hashCode based on author and name
     */
    @Override
    public int hashCode() {
        return Objects.hash(author, name);
    }
}
