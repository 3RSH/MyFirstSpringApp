package main.repository.tags;

import java.util.ArrayList;
import java.util.List;
import main.model.TagBinding;
import org.springframework.stereotype.Service;

@Service("TagBindingsStorage")
public class TagBindingsStorage implements TagBindingsRepository {

  private final PostgresTagBindingsRepository sqlTagBindingsRepository;


  public TagBindingsStorage(
      PostgresTagBindingsRepository sqlTagBindingsRepository) {
    this.sqlTagBindingsRepository = sqlTagBindingsRepository;
  }


  @Override
  public List<TagBinding> getAllTagBindings() {
    List<TagBinding> tagBindings = new ArrayList<>();
    sqlTagBindingsRepository.findAll().forEach(tagBindings::add);
    return tagBindings;
  }
}