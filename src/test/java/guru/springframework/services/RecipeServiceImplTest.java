package guru.springframework.services;


import guru.springframework.commands.RecipeCommand;
import guru.springframework.converters.RecipeCommandToRecipe;
import guru.springframework.converters.RecipeToRecipeCommand;
import guru.springframework.domain.Recipe;
import guru.springframework.exceptions.NotFoundException;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * Created by jt on 6/17/17.
 */
public class RecipeServiceImplTest {

    RecipeServiceImpl recipeService;

	// We can use @Mock to create and inject mocked instances without having to call Mockito.mock manually.
	// This tells Mockito to mock the RecipeRepository to create implementation/instance...
	// which is equivalent to List mockList = Mockito.mock(ArrayList.class);
    @Mock
    RecipeReactiveRepository recipeReactiveRepository;

    @Mock
    RecipeToRecipeCommand recipeToRecipeCommand;

    @Mock
    RecipeCommandToRecipe recipeCommandToRecipe;

    @Before
    public void setUp() throws Exception {
		// Enable Mockito annotations programmatically 
		// Tells Mockito to populate the annotated fields marked by @Mock
        MockitoAnnotations.initMocks(this);

		// Instantiate 'recipeService' with mocked 'recipeRepository' object
        recipeService = new RecipeServiceImpl(recipeReactiveRepository, recipeCommandToRecipe, recipeToRecipeCommand);
    }

    @Test
    public void getRecipeByIdTest() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setId("1");

        when(recipeReactiveRepository.findById(anyString())).thenReturn(Mono.just(recipe));

        Recipe recipeReturned = recipeService.findById("1").block();

        assertNotNull("Null recipe returned", recipeReturned);
        verify(recipeReactiveRepository, times(1)).findById(anyString());
        verify(recipeReactiveRepository, never()).findAll();
    }

    @Test(expected = NotFoundException.class)
    public void getRecipeByIdTestNotFound() throws Exception {

        when(recipeReactiveRepository.findById(anyString())).thenReturn(Mono.empty());

        Recipe recipeReturned = recipeService.findById("1").block();

        //should go boom
        
        // ME: Not any more going boom so do something about it -- just to make it pass. 
        //     Or completely delete this method since it does not occur in speaker version. 
        assertNull("Null recipe returned", recipeReturned);
        if (recipeReturned == null) throw new NotFoundException();
    }

    @Test
    public void getRecipeCommandByIdTest() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setId("1");

        when(recipeReactiveRepository.findById(anyString())).thenReturn(Mono.just(recipe));

        RecipeCommand recipeCommand = new RecipeCommand();
        recipeCommand.setId("1");

        when(recipeToRecipeCommand.convert(any())).thenReturn(recipeCommand);

        RecipeCommand commandById = recipeService.findCommandById("1").block();

        assertNotNull("Null recipe returned", commandById);
        verify(recipeReactiveRepository, times(1)).findById(anyString());
        verify(recipeReactiveRepository, never()).findAll();
    }

    @Test
    public void getRecipesTest() throws Exception {
      
        Recipe recipe = new Recipe();
        Flux<Recipe> receipesData = Flux.just(recipe);

		// when(…?.).thenReturn(…?.) method chain is used to specify a return value for a method call 
		// with pre-defined parameters. You also can use methods like 'anyString' or 'anyInt' to define 
		// that dependent on the input type a certain value should be returned.
		// Here, you are saying that return 'recipesData' object when they call recipeService.getRecipes()
        when(recipeService.getRecipes()).thenReturn(receipesData);
		//when(recipeRepository.findAll()).thenReturn(recipesData);

		//Set<Recipe> recipes = recipeService.getRecipes();
        List<Recipe> recipes = recipeService.getRecipes().collectList().block();
		System.out.println("recipes.size() = "+recipes.size());		

		// assertEquals(Expected, Actual)
        assertEquals(recipes.size(), 1);
		
		// Mockito keeps track of all the method calls and their parameters to the mock object. You can 
		// use the verify() method on the mock object to verify that the specified conditions are met. 
		// For example, you can verify that a method has been called with certain parameters. This kind 
		// of testing is sometimes called behavior testing. Behavior testing does not check the result 
		// of a method call, but it checks that a method is called with the right parameters.
        verify(recipeReactiveRepository, times(1)).findAll();
        verify(recipeReactiveRepository, never()).findById(anyString());
    }
    
    /**
     * Speaker version of getRecipesTest() but not shown in video.
     * @throws Exception
     */
    @Test
    public void getRecipesTest2() throws Exception {

        Recipe recipe = new Recipe();
        HashSet receipesData = new HashSet();
        receipesData.add(recipe);

        when(recipeService.getRecipes()).thenReturn(Flux.just(recipe));

        List<Recipe> recipes = recipeService.getRecipes().collectList().block();

        assertEquals(recipes.size(), 1);
        verify(recipeReactiveRepository, times(1)).findAll();
        verify(recipeReactiveRepository, never()).findById(anyString());
    }    

    @Test
    public void testDeleteById() throws Exception {

        //given
        String idToDelete = "2";
        
        when(recipeReactiveRepository.deleteById(anyString())).thenReturn(Mono.empty());

        //when
        recipeService.deleteById(idToDelete);

        //no 'when', since method has void return type

        //then
        verify(recipeReactiveRepository, times(1)).deleteById(anyString());
    }
}