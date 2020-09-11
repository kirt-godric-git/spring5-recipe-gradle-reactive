package guru.springframework.services;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.converters.IngredientCommandToIngredient;
import guru.springframework.converters.IngredientToIngredientCommand;
import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import guru.springframework.repositories.reactive.UnitOfMeasureReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Created by jt on 6/28/17.
 */
@Slf4j
@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeReactiveRepository recipeReactiveRepository;
    private final UnitOfMeasureReactiveRepository unitOfMeasureRepository;

    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand,
                                 IngredientCommandToIngredient ingredientCommandToIngredient,
                                 RecipeReactiveRepository recipeReactiveRepository, UnitOfMeasureReactiveRepository unitOfMeasureRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.recipeReactiveRepository = recipeReactiveRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Override
    public Mono<IngredientCommand> findByRecipeIdAndIngredientId(String recipeId, String ingredientId) {

    	log.info("IngredientServiceImpl.findByRecipeIdAndIngredientId() Recipe Id: " + recipeId+" Ingredient Id: "+ingredientId);
    	
        return recipeReactiveRepository
        		.findById(recipeId)
        		.flatMapIterable(Recipe::getIngredients)
        		.filter(ingredient -> ingredient.getId().equalsIgnoreCase(ingredientId))
        		.single()
        		.map(ingredient -> {
        			IngredientCommand command = ingredientToIngredientCommand.convert(ingredient);
        			command.setRecipeId(recipeId);
        			System.out.println("command.getRecipeId() :: " + command.getRecipeId());
        			return command;
             });
        
    }

    @Override
    public Mono<IngredientCommand> saveIngredientCommand(IngredientCommand command) {
    	log.info("IngredientServiceImpl.saveIngredientCommand() ");
    	System.out.println("Recipe ID: "+command.getRecipeId());
    	System.out.println("Ingredient ID: "+command.getId());
        Recipe recipe = recipeReactiveRepository.findById(command.getRecipeId()).block();

        if(recipe == null){

            //todo toss error if not found!
            log.error("Recipe not found for id: " + command.getRecipeId());
            return Mono.just(new IngredientCommand());
            
        } else {
            System.out.println("Find Ingredient ID: "+command.getId()+" within Recipe list...");
            Optional<Ingredient> ingredientOptional = recipe
                    .getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(command.getId()))
                    .findFirst();

            if(ingredientOptional.isPresent()){
                Ingredient ingredientFound = ingredientOptional.get();
                ingredientFound.setDescription(command.getDescription());
                ingredientFound.setAmount(command.getAmount());
                ingredientFound.setUom(unitOfMeasureRepository
                        .findById(command.getUom().getId()).block());
                
                if (ingredientFound.getUom() == null){
                    new RuntimeException("UOM NOT FOUND");
                }
                System.out.println("Found Ingredient ID: "+command.getId()+" & updated its properties...");
            } else {
                //add new Ingredient
                Ingredient ingredient = ingredientCommandToIngredient.convert(command);
                recipe.addIngredient(ingredient);
                System.out.println("New Ingredient ID: "+command.getId()+" & updated its properties...");
            }

            Recipe savedRecipe = recipeReactiveRepository.save(recipe).block();
            System.out.println("Saved Recipe ID: "+command.getRecipeId()+" including Ingredient property...");
            Optional<Ingredient> savedIngredientOptional = savedRecipe.getIngredients().stream()
                    .filter(recipeIngredients -> recipeIngredients.getId().equals(command.getId()))
                    .findFirst();

            //check by description
            if(!savedIngredientOptional.isPresent()){
                //not totally safe... But best guess
                savedIngredientOptional = savedRecipe.getIngredients().stream()
                        .filter(recipeIngredients -> recipeIngredients.getDescription().equals(command.getDescription()))
                        .filter(recipeIngredients -> recipeIngredients.getAmount().equals(command.getAmount()))
                        .filter(recipeIngredients -> recipeIngredients.getUom().getId().equals(command.getUom().getId()))
                        .findFirst();
                System.out.println("Found saved Ingredient ID: "+savedIngredientOptional.get().getId());
            } else {
            	System.out.println("NOT Found saved Ingredient ID: "+savedIngredientOptional.get().getId());
            }

            //to do check for fail
            
            //enhance with id value
            IngredientCommand ingredientCommandSaved = ingredientToIngredientCommand.convert(savedIngredientOptional.get());
            ingredientCommandSaved.setRecipeId(recipe.getId());
            
            return Mono.just(ingredientCommandSaved);
        }

    }

    @Override
    public Mono<Void> deleteById(String recipeId, String idToDelete) {
    	
        log.debug("Deleting ingredient: " + recipeId + ":" + idToDelete);

        Recipe recipe = recipeReactiveRepository.findById(recipeId).block();

        if(recipe != null){
        	
            log.debug("found recipe");

            Optional<Ingredient> ingredientOptional = recipe
                    .getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(idToDelete))
                    .findFirst();

            if(ingredientOptional.isPresent()){
                log.debug("found Ingredient");
                //Ingredient ingredientToDelete = ingredientOptional.get();
                // Remove or set NULL to Recipe parent reference to this Ingredient
                //ingredientToDelete.setRecipe(null);
                // Remove this ingredient from Recipe object's list
                recipe.getIngredients().remove(ingredientOptional.get());
                recipeReactiveRepository.save(recipe).block();
            }
        } else {
            log.debug("Recipe Id Not found. Id:" + recipeId);
        }
        
        return Mono.empty();
    }
}
