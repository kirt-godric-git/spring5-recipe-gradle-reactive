package guru.springframework.controllers;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.commands.RecipeCommand;
import guru.springframework.commands.UnitOfMeasureCommand;
import guru.springframework.services.IngredientService;
import guru.springframework.services.RecipeService;
import guru.springframework.services.UnitOfMeasureService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jt on 6/28/17.
 */
@Slf4j
@Controller
public class IngredientController {

    private final IngredientService ingredientService;
    private final RecipeService recipeService;
    private final UnitOfMeasureService unitOfMeasureService;

    public IngredientController(IngredientService ingredientService, RecipeService recipeService, UnitOfMeasureService unitOfMeasureService) {
        this.ingredientService = ingredientService;
        this.recipeService = recipeService;
        this.unitOfMeasureService = unitOfMeasureService;
    }

    @GetMapping("/recipe/{recipeId}/ingredients")
    public String listIngredients(@PathVariable String recipeId, Model model){
    	log.info("IngredientController.listIngredients(): Getting ingredient list for recipe id: " + recipeId);

        // use command object to avoid lazy load errors in Thymeleaf.
    	// Creating "block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-2"
        //model.addAttribute("recipe", recipeService.findCommandById(recipeId).block());
        model.addAttribute("recipe", recipeService.findCommandById(recipeId));

        return "recipe/ingredient/list";
    }

    @GetMapping("recipe/{recipeId}/ingredient/{id}/show")
    public String showRecipeIngredient(@PathVariable String recipeId,
                                       @PathVariable String id, Model model){
    	log.info("IngredientController.showRecipeIngredient(): Recipe id: " + recipeId+ " Ingredient Id: "+id);
        //model.addAttribute("ingredient", ingredientService.findByRecipeIdAndIngredientId(recipeId, id).block());
        model.addAttribute("ingredient", ingredientService.findByRecipeIdAndIngredientId(recipeId, id));
        return "recipe/ingredient/show";
    }

    @GetMapping("recipe/{recipeId}/ingredient/new")
    public String newRecipeIngredient(@PathVariable String recipeId, Model model){
    	log.info("IngredientController.newRecipeIngredient(): Recipe id: " + recipeId);

        //make sure we have a good id value
        RecipeCommand recipeCommand = recipeService.findCommandById(recipeId).block();
        //todo raise exception if null

        //need to return back parent id for hidden form property
        IngredientCommand ingredientCommand = new IngredientCommand();
        ingredientCommand.setRecipeId(recipeId);
        model.addAttribute("ingredient", ingredientCommand);

        //init uom
        ingredientCommand.setUom(new UnitOfMeasureCommand());

        //model.addAttribute("uomList",  unitOfMeasureService.listAllUoms());
        model.addAttribute("uomList",  unitOfMeasureService.listAllUoms().collectList().block());

        return "recipe/ingredient/ingredientform";
    }

    @GetMapping("recipe/{recipeId}/ingredient/{id}/update")
    public String updateRecipeIngredient(@PathVariable String recipeId,
                                         @PathVariable String id, Model model){
    	log.info("IngredientController.updateRecipeIngredient(): Recipe id: " + recipeId+ " Ingredient Id: "+id);
    	
        //model.addAttribute("ingredient", ingredientService.findByRecipeIdAndIngredientId(recipeId, id).block());
    	model.addAttribute("ingredient", ingredientService.findByRecipeIdAndIngredientId(recipeId, id));
    	/*Mono<IngredientCommand> ingredientCommandMono = ingredientService.findByRecipeIdAndIngredientId(recipeId, id);
    	ingredientCommandMono.subscribe();
    	
    	log.info("ingredientCommandMono: Recipe id: " + ingredientCommandMono.block().getRecipeId()+ " Ingredient Id: "+ingredientCommandMono.block().getId());
        model.addAttribute("ingredient", ingredientCommandMono);*/

        //model.addAttribute("uomList", unitOfMeasureService.listAllUoms());
        //model.addAttribute("uomList",  unitOfMeasureService.listAllUoms().collectList().block());
        model.addAttribute("uomList",  unitOfMeasureService.listAllUoms().collectList());
        return "recipe/ingredient/ingredientform";
    }

    @PostMapping("recipe/{recipeId}/ingredient")
    public String saveOrUpdate(@ModelAttribute IngredientCommand command){
    	
    	log.info("IngredientController.saveOrUpdate(): Object Recipe id: " + command.getRecipeId()+ " Object Ingredient Id: "+command.getId());    	
        IngredientCommand savedCommand = ingredientService.saveIngredientCommand(command).block();

        log.info("saved receipe id:" + savedCommand.getRecipeId());
        log.info("saved ingredient id:" + savedCommand.getId());

        return "redirect:/recipe/" + savedCommand.getRecipeId() + "/ingredient/" + savedCommand.getId() + "/show";
    }

    @GetMapping("recipe/{recipeId}/ingredient/{id}/delete")
    public String deleteRecipeIngredient(@PathVariable String recipeId,
                                   @PathVariable String id){
    	log.info("IngredientController.deleteRecipeIngredient(): Recipe id: " + recipeId+ " Ingredient Id: "+id);
        ingredientService.deleteById(recipeId, id).block();

        return "redirect:/recipe/" + recipeId + "/ingredients";
    }
}
