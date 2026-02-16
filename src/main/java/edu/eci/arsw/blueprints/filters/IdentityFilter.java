package edu.eci.arsw.blueprints.filters;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.eci.arsw.blueprints.model.Blueprint;

/**
 * Filtro por defecto: retorna el blueprint sin modificaciones.
 * Se activa cuando no hay perfiles de filtro activos (perfil "default" o ninguno).
 */
@Component
@Profile("!redundancy & !undersampling")
public class IdentityFilter implements BlueprintsFilter {
    @Override
    public Blueprint apply(Blueprint bp) { return bp; }
}
