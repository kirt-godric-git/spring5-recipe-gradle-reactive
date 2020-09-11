package guru.springframework.controllers;

import guru.springframework.domain.Recipe;
import guru.springframework.services.RecipeService;
import reactor.core.publisher.Flux;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Created by jt on 6/17/17.
 */
public class IndexControllerTest {

	// Tells Mockito to mock (create implementation/instance) of the 'recipeService' 
    @Mock
    RecipeService recipeService;

	// Tells Mockito to mock (create implementation/instance) of the 'model' 
    @Mock
    Model model;

    IndexController controller;

    @Before
    public void setUp() throws Exception {
		
		// Tells Mockito to populate the annotated fields marked by @Mock
        MockitoAnnotations.initMocks(this);

		// Instantiate IndexController with mocked recipeService
        controller = new IndexController(recipeService);
    }

    @Test
    public void testMockMVC() throws Exception {

        //given -- My code to avoid NPE when you call mockMvc.perform(get("/"))      
    	Set<Recipe> recipes = new HashSet<>();
    	recipes.add(new Recipe());
    	
        Recipe recipe = new Recipe();
        recipe.setId("1");
        recipes.add(recipe);

        when(recipeService.getRecipes()).thenReturn(Flux.fromIterable(recipes));
        
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
		/* // OR Instead of above recipes setup and when use this: 
		when(recipeService.getRecipes()).thenReturn(Flux.empty());
		*/
		
		mockMvc.perform(get("/"))
		        .andExpect(status().isOk())
		        .andExpect(view().name("index"));
    }

    @Test
    public void getIndexPage() throws Exception {

        //given     
    	Set<Recipe> recipes = new HashSet<>();
    	recipes.add(new Recipe());

        Recipe recipe = new Recipe();
        recipe.setId("1");

        recipes.add(recipe);

        when(recipeService.getRecipes()).thenReturn(Flux.fromIterable(recipes));
        
        // Original code:
        //ArgumentCaptor<Set<Recipe>> argumentCaptor = ArgumentCaptor.forClass(Set.class);	
        // From Speaker but NOT Working
        //ArgumentCaptor<Flux<Recipe>> argumentCaptor = ArgumentCaptor.forClass(Flux.class);	
        // My own code changes for reactive instead of above to make it work.
        ArgumentCaptor<List<Recipe>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        //when: Call controller.getIndexPage() & pass mocked 'model'
        String viewName = controller.getIndexPage(model);


        //then: Verify if 'recipeService.getRecipes()' was called only 1 times
        assertEquals("index", viewName);
        verify(recipeService, times(1)).getRecipes();
        verify(model, times(1)).addAttribute(eq("recipes"), argumentCaptor.capture());
        
        /* From Speaker but NOT Working
        Flux<Recipe> fluxInController = argumentCaptor.getValue();
        List<Recipe> recipeList = fluxInController.collectList().block();
        assertEquals(2, recipeList.size());*/
        
        // My own code changes for reactive instead of above to make it work.
        List<Recipe> setInController = argumentCaptor.getValue();
        assertEquals(2, setInController.size());
    }

}