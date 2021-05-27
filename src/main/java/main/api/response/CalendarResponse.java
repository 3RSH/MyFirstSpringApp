package main.api.response;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class CalendarResponse {

  @Getter
  @Setter
  private List<Integer> years;

  @Getter
  @Setter
  private Map<String, Integer> posts;
}