package main.security;

import lombok.Getter;

public enum Permission {

  USE("use"),
  MODERATE("moderate");


  @Getter
  private final String permission;


  Permission(String permission) {
    this.permission = permission;
  }
}