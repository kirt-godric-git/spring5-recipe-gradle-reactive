package guru.springframework.repositories;

import guru.springframework.bootstrap.RecipeBootstrap;
import guru.springframework.domain.UnitOfMeasure;
import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Created by jt on 6/17/17.
 */
@Slf4j
//@Ignore
@RunWith(SpringRunner.class)
//@DataJpaTest
@DataMongoTest
public class UnitOfMeasureRepositoryIT {

    @Autowired
    UnitOfMeasureRepository unitOfMeasureRepository;
    
    @Autowired
    CategoryRepository categoryRepository;
    
    @Autowired
    RecipeRepository recipeRepository;

    @Before
    public void setUp() throws Exception {
    	log.info("setUp() called");
    	
    	// Speaker: To reset the database records content
    	log.info("Deleting all data....");
    	recipeRepository.deleteAll();
    	unitOfMeasureRepository.deleteAll();
    	categoryRepository.deleteAll();
    	
    	// Speaker: Mimicking what Spring would do for us in the context.
    	log.info("Initialize database records....");
    	RecipeBootstrap recipeBootstrap = new RecipeBootstrap(categoryRepository, recipeRepository, unitOfMeasureRepository);
    	
    	// Speaker: We just wanna call that on application on that and we're not using that context in there.
    	// So I'm just gonna pass at it, event in as null, so we're not checking that but it'll trigger
    	// all these others to load this.
    	recipeBootstrap.onApplicationEvent(null);
    }

    @Test
    public void findByDescription() throws Exception {
    	log.info("findByDescription() called");
    	
        Optional<UnitOfMeasure> uomOptional = unitOfMeasureRepository.findByDescription("Teaspoon");

		assertEquals("Teaspoon", uomOptional.get().getDescription());		// Success
		//assertEquals("Teaspoonsss", uomOptional.get().getDescription());	// Fail
    }

    @Test
    public void findByDescriptionCup() throws Exception {
    	log.info("findByDescriptionCup() called");
    	
        Optional<UnitOfMeasure> uomOptional = unitOfMeasureRepository.findByDescription("Cup");

		assertEquals("Cup", uomOptional.get().getDescription());		// Success
    }

}