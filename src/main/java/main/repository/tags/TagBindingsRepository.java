package main.repository.tags;

import java.util.List;
import main.model.TagBinding;

public interface TagBindingsRepository {

  TagBinding getTagBinding(int tagBindingId);
  List<TagBinding> getAllTagBindings();

}
