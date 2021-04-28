package main.repository.tags;

import java.util.List;
import main.model.TagBinding;

public interface TagBindingsRepository {

  List<TagBinding> getAllTagBindings();
}