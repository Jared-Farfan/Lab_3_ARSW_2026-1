package edu.eci.arsw.blueprints.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Blueprint {

    private String author;
    private String name;
    private final List<Point> points = new ArrayList<>();
    /* Constructor for Blueprint
    */
    public Blueprint(String author, String name, List<Point> pts) {
        this.author = author;
        this.name = name;
        if (pts != null) points.addAll(pts);
    }
    /* Returns the author of the blueprint
     */
    public String getAuthor() { return author; }
    /* Returns the name of the blueprint
     */
    public String getName() { return name; }
    /* Returns an unmodifiable view of the points list
     */
    public List<Point> getPoints() { return Collections.unmodifiableList(points); }
    /* Adds a point to the blueprint
     */
    public void addPoint(Point p) { points.add(p); }
    /* Overrides equals and hashCode based on author and name
     */
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
