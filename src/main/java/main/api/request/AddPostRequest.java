package main.api.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class AddPostRequest {

  @Getter
  @Setter
  long timestamp;

  @Getter
  @Setter
  short active;

  @Getter
  @Setter
  String title;

  @Getter
  @Setter
  List<String> tags;

  @Getter
  @Setter
  String text;
}