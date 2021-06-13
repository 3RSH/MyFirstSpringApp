package main.security;

import lombok.Getter;

public enum Permission {

  USE("user:write"),
  MODERATE("user:moderate");


  @Getter
  private final String permission;


  Permission(String permission) {
    this.permission = permission;
  }
}