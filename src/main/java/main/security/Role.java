package main.security;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {

  USER(Set.of(Permission.USE)),
  MODERATOR(Set.of(Permission.USE, Permission.MODERATE));


  @Getter
  private final Set<Permission> permissions;


  Role(Set<Permission> permissions) {
    this.permissions = permissions;
  }


  public Set<SimpleGrantedAuthority> getAuthorities() {
    return permissions.stream()
        .map(p -> new SimpleGrantedAuthority(p.getPermission()))
        .collect(Collectors.toSet());
  }
}