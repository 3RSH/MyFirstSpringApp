package main.repository.tags;

import main.model.TagBinding;
import org.springframework.data.repository.CrudRepository;

public interface PostgresTagBindingsRepository extends CrudRepository<TagBinding, Integer> {

}
