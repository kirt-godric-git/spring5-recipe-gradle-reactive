package guru.springframework.repositories.reactive;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import guru.springframework.domain.UnitOfMeasure;
import reactor.core.publisher.Mono;

public interface UnitOfMeasureReactiveRepository extends ReactiveMongoRepository <UnitOfMeasure, String> {
	Mono<UnitOfMeasure> findByDescription(String description);
}
