package main.api.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InitResponse {

  @Getter
  @Setter
  @Value("${blog.title}")
  private String title;

  @Getter
  @Setter
  @Value("${blog.subtitle}")
  private String subtitle;

  @Getter
  @Setter
  @Value("${blog.phone}")
  private String phone;

  @Getter
  @Setter
  @Value("${blog.email}")
  private String email;

  @Getter
  @Setter
  @Value("${blog.copyright}")
  private String copyright;

  @Getter
  @Setter
  @Value("${blog.copyrightFrom}")
  private String copyrightFrom;
}