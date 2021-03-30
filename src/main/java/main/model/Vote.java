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
@Table(name = "post_votes")
public class Vote {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Getter
  @Setter
  private int id;

  @Column(name = "user_id")
  @Getter
  @Setter
  private int userId;

  @Column(name = "post_id")
  @Getter
  @Setter
  private int postId;

  @Getter
  @Setter
  private Date time;

  @Column(name = "is_active")
  @Getter
  @Setter
  private int value;
}
