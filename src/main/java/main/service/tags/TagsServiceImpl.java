package main.service.tags;

import java.util.ArrayList;
import java.util.List;
import main.api.TagInfo;
import main.api.response.TagResponse;
import main.model.Post;
import main.model.Post.ModerationStatusType;
import main.model.Tag;
import main.model.TagBinding;
import main.repository.posts.PostsRepository;
import main.repository.tags.TagBindingsRepository;
import main.repository.tags.TagsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TagsServiceImpl implements TagsService {

  private final TagsRepository tagsStorage;
  private final TagBindingsRepository tagBindingsStorage;
  private final PostsRepository postStorage;


  public TagsServiceImpl(@Qualifier("TagsStorage") TagsRepository tagsStorage,
      @Qualifier("TagBindingsStorage") TagBindingsRepository tagBindingsStorage,
      @Qualifier("PostsStorage") PostsRepository postStorage) {
    this.tagsStorage = tagsStorage;
    this.tagBindingsStorage = tagBindingsStorage;
    this.postStorage = postStorage;
  }


  @Override
  public TagResponse getTags() {
    List<Tag> tags = tagsStorage.getAllTags();
    List<TagBinding> tagBindings = tagBindingsStorage.getAllTagBindings();

    List<Float> weights = getDWeights(tags, tagBindings, getAvailablePostCount(postStorage));

    weights = getWeights(weights);

    TagResponse tagResponse = new TagResponse();
    tagResponse.setTags(getTagInfoList(tags, weights));

    return tagResponse;
  }

  @Override
  public TagResponse getTag(String query) {
    List<Tag> tags = tagsStorage.getAllTags();
    List<TagBinding> tagBindings = tagBindingsStorage.getAllTagBindings();

    List<Float> weights = getDWeights(tags, tagBindings, getAvailablePostCount(postStorage));

    weights = getWeights(weights);

    TagResponse tagResponse = new TagResponse();
    tagResponse.setTags(getTagInfoList(tags, weights, query));

    return tagResponse;
  }


  private int getAvailablePostCount(PostsRepository postStorage) {
    int availablePostCount = 0;
    long postCount = postStorage.count();

    for (int i = 1; i <= postCount; i++) {
      Post post = postStorage.getPost(i);

      if (isAvailable(post)) {
        availablePostCount++;
      }
    }

    return availablePostCount;
  }

  private boolean isAvailable(Post post) {
    return post.getIsActive() == 1
        && post.getModerationStatus() == ModerationStatusType.ACCEPTED
        && post.getTime().getTime() <= System.currentTimeMillis();
  }

  private List<Float> getDWeights(List<Tag> tags, List<TagBinding> tagBindings
      , int availablePostCount) {

    List<Float> dWeights = new ArrayList<>();

    for (Tag tag : tags) {
      int tagCount = 0;

      for (TagBinding tagBinding : tagBindings) {
        Post post = tagBinding.getPost();

        if (isAvailable(post) && tagBinding.getTag().equals(tag)) {
          tagCount++;
        }
      }

      float dWeight = (float) tagCount / availablePostCount;
      dWeights.add(dWeight);
    }

    return dWeights;
  }

  private List<Float> getWeights(List<Float> dWeights) {
    float factor = getNormalFactor(dWeights);
    List<Float> weights = new ArrayList<>();

    for (Float dWeight : dWeights) {
      float weight = dWeight * factor * 100;
      weight = (float) (int) ((weight - (int) weight) >= 0.5F
          ? weight + 1
          : weight) / 100;

      weights.add(weight);
    }

    return weights;
  }

  private float getNormalFactor(List<Float> dWeights) {
    float maxDWeight = 0;

    for (float dWeight : dWeights) {
      if (maxDWeight < dWeight) {
        maxDWeight = dWeight;
      }
    }

    return 1 / maxDWeight;
  }

  private List<TagInfo> getTagInfoList(List<Tag> tags, List<Float> weights) {
    List<TagInfo> tagInfoList = new ArrayList<>();

    for (int i = 0; i < tags.size(); i++) {
      TagInfo tag = new TagInfo();
      tag.setName(tags.get(i).getName());
      tag.setWeight(weights.get(i));
      tagInfoList.add(tag);
    }

    return tagInfoList;
  }

  private List<TagInfo> getTagInfoList(List<Tag> tags, List<Float> weights, String query) {
    List<TagInfo> tagInfoList = new ArrayList<>();

    for (int i = 0; i < tags.size(); i++) {
      TagInfo tag = new TagInfo();

      if (tags.get(i).getName().matches(query + ".*")) {
        tag.setName(tags.get(i).getName());
        tag.setWeight(weights.get(i));
        tagInfoList.add(tag);
      }
    }

    return tagInfoList;
  }
}