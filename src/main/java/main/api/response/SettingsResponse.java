package main.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class SettingsResponse {

  @Getter
  @Setter
  @JsonProperty("MULTIUSER_MODE")
  private boolean multiuserMode;

  @Getter
  @Setter
  @JsonProperty("POST_PREMODERATION")
  private boolean postPremoderation;

  @Getter
  @Setter
  @JsonProperty("STATISTICS_IS_PUBLIC")
  private boolean statisticsIsPublic;
}