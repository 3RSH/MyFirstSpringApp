package main.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
public class User {

  @Id
  @SequenceGenerator(name = "usersIdSeq", sequenceName = "users_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usersIdSeq")
  @Getter
  @Setter
  private int id;

  @Column(name = "is_moderator", nullable = false)
  @Getter
  @Setter
  private short isModerator;

  @Column(name = "reg_time", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
  @Getter
  @Setter
  private Timestamp regTime;

  @Column(nullable = false)
  @Getter
  @Setter
  private String name;

  @Column(nullable = false)
  @Getter
  @Setter
  private String email;

  @Column(nullable = false)
  @Getter
  @Setter
  private String password;

  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "TEXT")
  @Getter
  @Setter
  private String photo;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Getter
  @Setter
  private Set<Post> posts = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Getter
  @Setter
  private Set<Vote> votes = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Getter
  @Setter
  private Set<PostComment> comments = new HashSet<>();
}