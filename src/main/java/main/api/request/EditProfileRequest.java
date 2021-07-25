package main.api.request;

import lombok.Getter;
import lombok.Setter;

public class EditProfileRequest {

  @Getter
  @Setter
  String name;

  @Getter
  @Setter
  String email;

  @Getter
  @Setter
  String password;

  @Getter
  @Setter
  short removePhoto;

  @Getter
  @Setter
  String photo;
}