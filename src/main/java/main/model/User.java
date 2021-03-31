package main.model;

import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Getter
  @Setter
  private int id;

  @Column(name = "is_moderator", nullable = false)
  @Getter
  @Setter
  private short isModerator;

  @Column(name = "reg_time", nullable = false)
  @Getter
  @Setter
  private Date regTime;

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

  @Getter
  @Setter
  private String photo;
}