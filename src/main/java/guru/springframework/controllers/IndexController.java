package guru.springframework.controllers;

import guru.springframework.domain.Recipe;
import guru.springframework.services.RecipeService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jt on 6/1/17.
 */
@Slf4j
@Controller
public class IndexController {

    private final RecipeService recipeService;
    
    Model model;

    public IndexController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @RequestMapping({"", "/", "/index"})
    public String getIndexPage(Model model) {
        log.debug("Getting Index page");

        // Creating "block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-2"
        //model.addAttribute("recipes", recipeService.getRecipes().collectList().block());
        model.addAttribute("recipes", recipeService.getRecipes().collectList());

        return "index";
    }
    
    private static void handleResponse(Mono<List<Recipe>> recipeMonoList) {
        System.out.println("handle response");
        //System.out.println(s);
    }
}
