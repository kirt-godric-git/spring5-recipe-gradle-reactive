package guru.springframework.repositories;

import guru.springframework.domain.Recipe;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by jt on 6/13/17.
 */
public interface RecipeRepository extends CrudRepository<Recipe, String> {
	Optional<Recipe> findByDescription(String description);
}
