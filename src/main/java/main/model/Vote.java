package main.model;

import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post_votes")
public class Vote {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Getter
  @Setter
  private int id;

  @OneToMany
  @Column(name = "user_id", nullable = false)
  @JoinColumn(name = "id")
  @Getter
  @Setter
  private User user;

  @OneToMany(fetch = FetchType.LAZY)
  @Column(name = "post_id", nullable = false)
  @JoinColumn(name = "id")
  @Getter
  @Setter
  private Post post;

  @Getter
  @Setter
  private Date time;

  @Column(name = "is_active")
  @Getter
  @Setter
  private int value;
}
