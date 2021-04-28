package main.service.tags;

import main.api.response.TagResponse;

public interface TagsService {

  TagResponse getTags();
  TagResponse getTag(String query);

}
