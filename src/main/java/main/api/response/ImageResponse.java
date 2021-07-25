package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import main.api.ImageErrors;

public class ImageResponse {

  @Getter
  @Setter
  private boolean result;

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  private String imagePath;

  @Getter
  @Setter
  @JsonProperty("errors")
  @JsonInclude(Include.NON_NULL)
  private ImageErrors imageErrors;
}