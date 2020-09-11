package guru.springframework.services;

import guru.springframework.commands.RecipeCommand;
import guru.springframework.converters.RecipeCommandToRecipe;
import guru.springframework.converters.RecipeToRecipeCommand;
import guru.springframework.domain.Recipe;
import guru.springframework.exceptions.NotFoundException;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by jt on 6/13/17.
 */
@Slf4j
@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeReactiveRepository recipeReactiveRepository;
    private final RecipeCommandToRecipe recipeCommandToRecipe;
    private final RecipeToRecipeCommand recipeToRecipeCommand;

    public RecipeServiceImpl(RecipeReactiveRepository recipeRepository, RecipeCommandToRecipe recipeCommandToRecipe, RecipeToRecipeCommand recipeToRecipeCommand) {
        this.recipeReactiveRepository = recipeRepository;
        this.recipeCommandToRecipe = recipeCommandToRecipe;
        this.recipeToRecipeCommand = recipeToRecipeCommand;
    }

    @Override
    public Flux<Recipe> getRecipes() {
        log.info("I'm in the RecipeServiceImpl.getRecipes()");
        
        return recipeReactiveRepository.findAll();
    }

    @Override
    public Mono<Recipe> findById(String id) {

    	log.info("I'm in the RecipeServiceImpl.findById(): Recipe id = "+id);
    	Mono<Recipe> recipe = recipeReactiveRepository.findById(id);
        return recipe;
    }

    @Override
    //@Transactional
    public Mono<RecipeCommand> findCommandById(String id) {
    	log.info("I'm in the RecipeServiceImpl.findCommandById(): Recipe id = "+id);
    	
    	return recipeReactiveRepository.findById(id)
    			.map(recipe -> {
    				RecipeCommand recipeCommand = recipeToRecipeCommand.convert(recipe);
    				
    				recipeCommand.getIngredients().forEach(rc -> {
    	    			rc.setRecipeId(recipeCommand.getId());
    	    		});
    				
    				return recipeCommand;
    			});
    }

    @Override
    //@Transactional
    public Mono<RecipeCommand> saveRecipeCommand(RecipeCommand command) {
    	log.info("I'm in the RecipeServiceImpl.saveRecipeCommand(): ");
        
        return recipeReactiveRepository.save(recipeCommandToRecipe.convert(command))
        		.map(recipeToRecipeCommand::convert);
    }

    @Override
    public Mono<Void> deleteById(String idToDelete) {
    	log.info("I'm in the RecipeServiceImpl.deleteById(): Recipe id = "+idToDelete);
    	
        recipeReactiveRepository.deleteById(idToDelete).block();
        
        return Mono.empty();
    }
}
